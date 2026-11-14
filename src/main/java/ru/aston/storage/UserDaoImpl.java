package ru.aston.storage;

import lombok.RequiredArgsConstructor;
import org.hibernate.QueryTimeoutException;
import org.hibernate.SessionFactory;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.exception.LockAcquisitionException;
import ru.aston.exceptions.StorageUnavailableException;
import ru.aston.model.User;

import java.util.function.Supplier;

@RequiredArgsConstructor
public class UserDaoImpl implements UserDao {
    private final SessionFactory sessionFactory;

    @Override
    public User create(User user) {
        return execute(() -> sessionFactory.fromTransaction(session -> {
            session.persist(user);
            return user;
        }));
    }

    @Override
    public User update(User user) {
        return execute(() -> sessionFactory.fromTransaction(session ->
                session.merge(user)));
    }

    @Override
    public User findById(Long id) {
        return execute(() -> sessionFactory.fromSession(session ->
                session.find(User.class, id)));
    }

    @Override
    public void deleteById(Long id) {
        execute(() -> {
            sessionFactory.inTransaction(session -> {
                User user = session.find(User.class, id);
                if (user != null) {
                    session.remove(user);
                }
            });
            return null;
        });
    }

    @Override
    public boolean existsByEmail(String email) {
        return execute(() -> sessionFactory.fromSession(session -> {
            Long emailCount = session
                    .createQuery("select count(u) from User u where u.email = :email", Long.class)
                    .setParameter("email", email)
                    .getSingleResult();
            return emailCount > 0;
        }));
    }

    private <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (JDBCConnectionException e) {
            throw new StorageUnavailableException("Database is unavailable");
        } catch (LockAcquisitionException | QueryTimeoutException e) {
            throw new StorageUnavailableException("Database is busy, try again later");
        }
    }
}
