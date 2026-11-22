package IntegrationTests;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import ru.aston.model.User;

import static org.junit.jupiter.api.Assertions.*;

public class UserDAOTests extends BaseAbstractTest {
    private static final String EMAIL = "ivan@example.com";

    @Test
    void create_validUser_userCreated() {
        User created = userDao.create(validUser(EMAIL));

        assertNotNull(created.getUserId());
        User fromDb = userDao.findById(created.getUserId());
        assertNotNull(fromDb);
        assertAll(
                () -> assertEquals("Ivan", fromDb.getName()),
                () -> assertEquals(EMAIL, fromDb.getEmail()),
                () -> assertEquals(AGE, fromDb.getAge()),
                () -> assertEquals(CREATED_AT, fromDb.getCreatedAt()));
    }

    @Test
    void create_maxLengthFields_userCreated() {
        String name = "x".repeat(60);
        String email = "a".repeat(51) + "@test.com";

        User created = assertDoesNotThrow(() -> userDao.create(user(name, email, AGE)));

        assertNotNull(created.getUserId());
    }

    @Test
    void findById_unknownId_returnsNull() {
        assertNull(userDao.findById(-1L));
    }

    @Test
    void update_validChanges_userUpdated() {
        User created = userDao.create(validUser(EMAIL));
        User changes = user("Petr", "petr@example.com", (short) 41);
        changes.setUserId(created.getUserId());

        User updated = userDao.update(changes);

        assertEquals(created.getUserId(), updated.getUserId());
        User fromDb = userDao.findById(created.getUserId());
        assertAll(
                () -> assertEquals("Petr", fromDb.getName()),
                () -> assertEquals("petr@example.com", fromDb.getEmail()),
                () -> assertEquals((short) 41, fromDb.getAge()),
                () -> assertEquals(CREATED_AT, fromDb.getCreatedAt()));
        assertEquals(1, countUsers());
    }

    @Test
    void update_invalidEmail_emailRemainsUnchanged() {
        User created = userDao.create(validUser(EMAIL));
        User changes = user("Ivan", "broken", AGE);
        changes.setUserId(created.getUserId());

        assertThrows(
                ConstraintViolationException.class,
                () -> userDao.update(changes)
        );

        assertEquals(EMAIL, userDao.findById(created.getUserId()).getEmail());
    }

    @Test
    void deleteById_existingId_userDeleted() {
        User created = userDao.create(validUser(EMAIL));

        userDao.deleteById(created.getUserId());

        assertNull(userDao.findById(created.getUserId()));
        assertEquals(0, countUsers());
    }

    @Test
    void deleteById_unknownId_noExceptionThrown() {
        User created = userDao.create(validUser(EMAIL));

        assertDoesNotThrow(() -> userDao.deleteById(-1L));

        assertEquals(1, countUsers());
        assertNotNull(userDao.findById(created.getUserId()));
    }

    @Test
    void existsByEmail_whenEmailExists_returnTrue() {
        assertFalse(userDao.existsByEmail(EMAIL));

        userDao.create(validUser(EMAIL));

        assertTrue(userDao.existsByEmail(EMAIL));
        assertFalse(userDao.existsByEmail("other@example.com"));
    }
}
