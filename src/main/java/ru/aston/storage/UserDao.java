package ru.aston.storage;

import ru.aston.model.User;

public interface UserDao {

    User create(User user);

    User update(User user);

    User findById(Long id);

    void deleteById(Long id);

    boolean existsByEmail(String email);
}
