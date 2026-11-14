package ru.aston.storage;

import org.hibernate.Session;
import ru.aston.model.User;

public interface UserRepository {

    User create(Session session, User user);

    User update(Session session, User user);

    User findById(Session session, Long id);

    void deleteById(Session session, Long id);

    boolean existsByEmail(Session session, String email);
}
