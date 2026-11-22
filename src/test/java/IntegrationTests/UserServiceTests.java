package IntegrationTests;

import org.junit.jupiter.api.Test;
import ru.aston.exceptions.EmailConflictException;
import ru.aston.model.User;
import ru.aston.service.UserService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceTests extends BaseAbstractTest {
    private static final String EMAIL = "ivan@example.com";

    private UserService userService = new UserService(userDao);

    private static User newUser(String email) {
        User user = validUser(email);
        user.setCreatedAt(null);
        return user;
    }

    private static User changesFor(User existing, String name, String email) {
        User changes = user(name, email, (short) 41);
        changes.setUserId(existing.getUserId());
        changes.setCreatedAt(existing.getCreatedAt());
        return changes;
    }


    @Test
    void createUser_whenEmailDuplicated_throwsEmailConflictException() {
        userService.createUser(newUser(EMAIL));

        assertThrows(
                EmailConflictException.class,
                () -> userService.createUser(newUser(EMAIL))
        );

        assertEquals(1, countUsers());
    }

    @Test
    void updateUser_whenUserUpdateAllFieldsExceptedEmail_userShouldUpdated() {
        User created = userService.createUser(newUser(EMAIL));

        userService.updateUser(changesFor(created, "Petr", EMAIL));

        User fromDb = userService.findUserById(created.getUserId());
        assertEquals("Petr", fromDb.getName());
        assertEquals(EMAIL, fromDb.getEmail());
    }

    @Test
    void updateUser_whenUserUpdateEmail_emailShouldUpdated() {
        User created = userService.createUser(newUser(EMAIL));

        userService.updateUser(changesFor(created, "Ivan", "new@example.com"));

        assertEquals("new@example.com", userService.findUserById(created.getUserId()).getEmail());
    }

    @Test
    void updateUser_whenEmailBelongAnotherUser_throwsEmailConflictException() {
        User first = userService.createUser(newUser("first@example.com"));
        userService.createUser(newUser("second@example.com"));

        assertThrows(
                EmailConflictException.class,
                () -> userService.updateUser(changesFor(first, "Ivan", "second@example.com"))
        );

        assertEquals("first@example.com", userService.findUserById(first.getUserId()).getEmail());
    }

    @Test
    void updateUser_whenIdNotFound_throwsRuntimeException() {
        User notFound = user("notFound", "notFound@example.com", AGE);
        notFound.setUserId(-1L);

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userService.updateUser(notFound)
        );

        assertEquals("User not found", ex.getMessage());
        assertEquals(0, countUsers());
    }
}
