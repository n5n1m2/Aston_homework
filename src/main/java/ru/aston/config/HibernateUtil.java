package ru.aston.config;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import ru.aston.model.User;

public final class HibernateUtil {
    private static final SessionFactory SESSION_FACTORY = build();

    private static SessionFactory build() {
        return new Configuration()
                .addAnnotatedClass(User.class)
                .buildSessionFactory();
    }

    public static SessionFactory getSessionFactory() {
        return SESSION_FACTORY;
    }

    private HibernateUtil() {}
}
