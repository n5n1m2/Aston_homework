package ru.aston.storage;

import org.hibernate.Session;
import ru.aston.model.User;

public class UserRepositoryImpl implements UserRepository {
    @Override
    public User create(Session session, User user) {
        session.persist(user);
        return user;
    }

    @Override
    public User update(Session session, User user) {
        return session.merge(user);
    }

    @Override
    public User findById(Session session, Long id) {
        return session.find(User.class, id);
    }

    @Override
    public void deleteById(Session session, Long id) {
        User user = session.find(User.class, id);
        if (user != null) {
            session.remove(user);
        }
    }

    @Override
    public boolean existsByEmail(Session session, String email) {
        Long emailCount = session
                .createQuery("select count(u) from User u where u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult();
        return emailCount > 0;
    }
}
