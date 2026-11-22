package ru.aston.service;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.aston.exceptions.EmailConflictException;
import ru.aston.exceptions.ServiceTemporarilyUnavailable;
import ru.aston.exceptions.StorageUnavailableException;
import ru.aston.model.User;
import ru.aston.storage.UserDao;

import java.time.Instant;
import java.util.Objects;

@RequiredArgsConstructor
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserDao userDao;

    public User createUser(User user) {
        try {
            checkEmail(user);
            user.setCreatedAt(Instant.now());
            User created = userDao.create(user);
            logger.info("User created: {}", created);
            return created;
        } catch (StorageUnavailableException e) {
            throw unavailable(e);
        }
    }

    public User updateUser(User user) {
        try {
            User oldUser = userDao.findById(user.getUserId());
            if (oldUser == null) {
                throw new RuntimeException("User not found");
            }
            user.setCreatedAt(oldUser.getCreatedAt());
            logger.info("User updated. Old user:{}\nNew user:{}", oldUser, user);
            if (!Objects.equals(user.getEmail(), oldUser.getEmail())) {
                checkEmail(user);
            }
            return userDao.update(user);
        } catch (StorageUnavailableException e) {
            throw unavailable(e);
        }
    }

    public User findUserById(Long id) {
        try {
            logger.info("Find user by id: {}", id);
            return userDao.findById(id);
        } catch (StorageUnavailableException e) {
            throw unavailable(e);
        }
    }

    public void deleteUserById(Long id) {
        try {
            logger.info("Delete user by id: {}", id);
            userDao.deleteById(id);
        } catch (StorageUnavailableException e) {
            throw unavailable(e);
        }
    }

    private void checkEmail(User user) {
        logger.info("Checking email conflict for {}", user.getEmail());
        if (userDao.existsByEmail(user.getEmail())) {
            throw new EmailConflictException("Email already exists");
        }
    }

    private ServiceTemporarilyUnavailable unavailable(StorageUnavailableException e) {
        logger.error(e.getMessage(), e);
        return new ServiceTemporarilyUnavailable("Service unavailable");
    }
}
