package ru.aston.service;

import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.JDBCConnectionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aston.exceptions.EmailConflictException;
import ru.aston.exceptions.ServiceTemporarilyUnavailable;
import ru.aston.model.User;
import ru.aston.storage.UserRepository;

import java.time.Instant;
import java.util.Objects;

@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository repository;
    private final SessionFactory sessionFactory;

    public User createUser(User user) {
        try {
            return sessionFactory.fromTransaction(session -> {
                checkEmail(session, user);
                user.setCreatedAt(Instant.now());
                logger.info("User created: " + user);
                return repository.create(session, user);
            });
        } catch (JDBCConnectionException e) {
            logger.error(e.getMessage());
            throw new ServiceTemporarilyUnavailable("Service unavailable");
        }
    }

    public User updateUser(User user) {
        try {
            return sessionFactory.fromTransaction(session -> {
                User oldUser = repository.findById(session, user.getUserId());
                logger.info("User updated. Old user:{}\nNew user:{}", oldUser, user);
                if (!Objects.equals(user.getEmail(), oldUser.getEmail())) {
                    checkEmail(session, user);
                }
                return repository.update(session, user);
            });
        } catch (JDBCConnectionException e) {
            logger.error(e.getMessage());
            throw new ServiceTemporarilyUnavailable("Service unavailable");
        }
    }

    public User findUserById(Long id) {
        try {
            logger.info("Find user by id: {}", id);
            return sessionFactory.fromSession(session ->
                    repository.findById(session, id));
        } catch (JDBCConnectionException e) {
            logger.error(e.getMessage());
            throw new ServiceTemporarilyUnavailable("Service unavailable");
        }
    }

    public void deleteUserById(Long id) {
        try {
            logger.info("Delete user by id: {}", id);
            sessionFactory.inTransaction(session -> repository.deleteById(session, id));
        } catch (JDBCConnectionException e) {
            logger.error(e.getMessage());
            throw new ServiceTemporarilyUnavailable("Service unavailable");
        }
    }

    private void checkEmail(Session session, User user) {
        logger.info("Checking email conflict for {}", user.getEmail());
        if (repository.existsByEmail(session, user.getEmail())) {
            throw new EmailConflictException("Email already exists");
        }
    }
}
