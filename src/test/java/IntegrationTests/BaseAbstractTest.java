package IntegrationTests;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.aston.model.User;
import ru.aston.storage.UserDao;
import ru.aston.storage.UserDaoImpl;

import java.time.Instant;

public abstract class BaseAbstractTest extends PostgreSQL {
    static final Short AGE = 30;
    static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");
    static SessionFactory sessionFactory;
    static UserDao userDao;

    static SessionFactory buildSessionFactory(PostgreSQLContainer container) {
        return new Configuration()
                .addAnnotatedClass(User.class)
                .setProperty("hibernate.connection.driver_class", "org.postgresql.Driver")
                .setProperty("hibernate.connection.url", container.getJdbcUrl())
                .setProperty("hibernate.connection.username", container.getUsername())
                .setProperty("hibernate.connection.password", container.getPassword())
                .setProperty("hibernate.hbm2ddl.auto", "validate")
                .buildSessionFactory();
    }

    @BeforeAll
    static void startSessionFactory() {
        sessionFactory = buildSessionFactory(POSTGRESQL_CONTAINER);
        userDao = new UserDaoImpl(sessionFactory);
    }

    @AfterAll
    static void closeSessionFactory() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    static User user(String name, String email, Short age) {
        return User.builder()
                .name(name)
                .email(email)
                .age(age)
                .createdAt(CREATED_AT)
                .build();
    }

    static User validUser(String email) {
        return user("Ivan", email, AGE);
    }

    @BeforeEach
    void cleanUsers() {
        sessionFactory.inTransaction(session ->
                session.createMutationQuery("delete from User").executeUpdate());
    }

    static long countUsers() {
        return sessionFactory.fromSession(session ->
                session.createSelectionQuery("select count(u) from User u", Long.class)
                        .getSingleResult());
    }
}
