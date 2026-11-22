package unitTests;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.aston.exceptions.EmailConflictException;
import ru.aston.exceptions.StorageUnavailableException;
import ru.aston.model.User;
import ru.aston.service.UserService;
import ru.aston.storage.UserDao;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTests {

    private static final String EMAIL = "email@example.com";
    private static final String NEW_EMAIL = "new@example.com";
    private static final long ID = 1L;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private UserService userService;

    private static User user(Long id, String email) {
        return User.builder()
                .userId(id)
                .name("Ivan")
                .email(email)
                .age((short) 30)
                .build();
    }

    private static StorageUnavailableException storageDown() {
        return new StorageUnavailableException("");
    }

    @Nested
    class CreateUser {

        @Test
        void createUser_whenOk_ShouldReturnUser() {
            User input = user(null, EMAIL);
            when(userDao.existsByEmail(EMAIL)).thenReturn(false);
            when(userDao.create(input)).thenAnswer(inv -> inv.getArgument(0));

            Instant before = Instant.now();
            User result = userService.createUser(input);
            Instant after = Instant.now();

            assertSame(input, result);
            assertFalse(result.getCreatedAt().isBefore(before));
            assertFalse(result.getCreatedAt().isAfter(after));
        }


        @Test
        void createUser_whenEmailIsExists_throwException() {
            User input = user(null, EMAIL);
            when(userDao.existsByEmail(EMAIL)).thenReturn(true);

            EmailConflictException ex = assertThrows(EmailConflictException.class,
                    () -> userService.createUser(input));

            assertEquals("Email already exists", ex.getMessage());
            assertNull(input.getCreatedAt());
            verify(userDao, never()).create(any());
        }
    }

    @Nested
    class UpdateUser {
        @Test
        void userUpdate_whenEmailChangedAndFree_updateEmail() {
            User old = user(ID, EMAIL);
            User updated = user(ID, NEW_EMAIL);
            when(userDao.findById(ID)).thenReturn(old);
            when(userDao.existsByEmail(NEW_EMAIL)).thenReturn(false);
            when(userDao.update(updated)).thenReturn(updated);

            User result = userService.updateUser(updated);

            assertSame(updated, result);
            verify(userDao).existsByEmail(NEW_EMAIL);
            verify(userDao).update(updated);
        }

        @Test
        void userUpdate_whenEmailNotFree_throwException() {
            User old = user(ID, EMAIL);
            User updated = user(ID, NEW_EMAIL);
            when(userDao.findById(ID)).thenReturn(old);
            when(userDao.existsByEmail(NEW_EMAIL)).thenReturn(true);

            EmailConflictException ex = assertThrows(EmailConflictException.class,
                    () -> userService.updateUser(updated));

            assertEquals("Email already exists", ex.getMessage());
            verify(userDao, never()).update(any());
        }

        @Test
        void userUpdate_whenUserNotExists_throwException() {
            User updated = user(ID, EMAIL);
            when(userDao.findById(ID)).thenReturn(null);

            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> userService.updateUser(updated));

            assertEquals("User not found", ex.getMessage());
            verify(userDao).findById(ID);
            verifyNoMoreInteractions(userDao);
        }
    }

    @Nested
    class FindUserById {

        @Test
        void findUserById_whenUserExists_ShouldReturnUser() {
            User found = user(ID, EMAIL);
            when(userDao.findById(ID)).thenReturn(found);

            assertSame(found, userService.findUserById(ID));
        }

        @Test
        void findUserById_whenUserNotExists_returnNull() {
            when(userDao.findById(ID)).thenReturn(null);

            assertNull(userService.findUserById(ID));
        }
    }
}
