package ru.aston.controller;

import jakarta.validation.ConstraintViolationException;
import ru.aston.config.HibernateUtil;
import ru.aston.model.User;
import ru.aston.service.UserService;
import ru.aston.storage.UserDaoImpl;

import java.util.Scanner;

public class ConsoleApp {

    private final Scanner scanner = new Scanner(System.in);
    private final UserService service = new UserService(
            new UserDaoImpl(HibernateUtil.getSessionFactory())
    );

    public void start() {
        while (true) {
            printAvailableOperation();
            executeOperation();
        }
    }

    private void printAvailableOperation() {
        System.out.println("\nChoose operation:");
        System.out.println("1. Create new user.");
        System.out.println("2. Update user.");
        System.out.println("3. Get user by id.");
        System.out.println("4. Delete user.");
        System.out.println("0. Exit.");
    }

    private void executeOperation() {
        switch (scanner.nextLine().trim()) {
            case "1" -> createUser();
            case "2" -> updateUser();
            case "3" -> getUserById();
            case "4" -> deleteUser();
            case "0" -> {
                HibernateUtil.getSessionFactory().close();
                System.out.println("Application closed.");
                System.exit(0);
            }
            default -> System.out.println("Invalid operation");
        }
    }

    private void createUser() {
        System.out.println("Enter name:");
        String username = scanner.nextLine();
        if (username.isBlank()) {
            System.out.println("Invalid username");
            return;
        }

        System.out.println("Enter email:");
        String email = scanner.nextLine();
        if (email.isBlank()) {
            System.out.println("Email cannot be blank.");
            return;
        }

        Short age = readShort("Enter age:");

        if (age == null) {
            return;
        }

        User user = User.builder()
                .name(username)
                .email(email)
                .age(age)
                .build();

        try {
            User createdUser = service.createUser(user);
            System.out.println("User created: " + createdUser);
        } catch (ConstraintViolationException e) {
            e.getConstraintViolations()
                    .forEach(violation ->
                            System.out.println(violation.getMessage()));
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private void updateUser() {
        Long userId = readLong("Enter user id:");

        if (userId == null) {
            return;
        }

        User user = service.findUserById(userId);

        if (user == null) {
            System.out.println("User not found");
            return;
        }

        boolean needUpdate = true;

        while (needUpdate) {
            System.out.println("\nWhich field do you want to update?");
            System.out.println("1. Name: " + user.getName());
            System.out.println("2. Email: " + user.getEmail());
            System.out.println("3. Age: " + user.getAge());
            System.out.println("4. Save");
            System.out.println("0. Cancel");

            switch (scanner.nextLine().trim()) {
                case "1" -> {
                    System.out.println("Enter new name (empty to skip):");
                    String name = scanner.nextLine();

                    if (!name.isBlank()) {
                        user.setName(name);
                    }
                }
                case "2" -> {
                    System.out.println("Enter new email (empty to skip):");
                    String email = scanner.nextLine();

                    if (!email.isBlank()) {
                        user.setEmail(email);
                    }
                }
                case "3" -> {
                    Short age = readShort("Enter new age (empty to skip):");

                    if (age != null) {
                        user.setAge(age);
                    }
                }
                case "4" -> needUpdate = false;
                case "0" -> {
                    System.out.println("Update cancelled.");
                    return;
                }
                default -> System.out.println("Invalid operation");
            }
        }

        try {
            User updatedUser = service.updateUser(user);
            System.out.println("User updated: " + updatedUser);
        } catch (ConstraintViolationException e) {
            e.getConstraintViolations()
                    .forEach(violation ->
                            System.out.println(violation.getMessage()));
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private void getUserById() {
        Long userId = readLong("Enter user id:");

        if (userId == null) {
            return;
        }

        try {
            User user = service.findUserById(userId);

            if (user == null) {
                System.out.println("User not found");
                return;
            }

            System.out.println(user);
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private void deleteUser() {
        Long userId = readLong("Enter user id:");

        if (userId == null) {
            return;
        }

        try {
            User user = service.findUserById(userId);

            if (user == null) {
                System.out.println("User not found");
                return;
            }

            System.out.println("Are you sure you want to delete this user?");
            System.out.println(user);
            System.out.println("1. Yes");
            System.out.println("2. No");

            if (!scanner.nextLine().trim().equals("1")) {
                System.out.println("Deletion cancelled.");
                return;
            }

            service.deleteUserById(userId);
            System.out.println("User deleted.");

        } catch (RuntimeException e) {
            System.out.println(e.getMessage());
        }
    }

    private Short readShort(String message) {
        while (true) {
            System.out.println(message);

            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Age is required.");
                continue;
            }

            try {
                return Short.parseShort(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number. Please enter a valid short integer.");
            }
        }
    }

    private Long readLong(String message) {
        while (true) {
            System.out.println(message);

            String input = scanner.nextLine().trim();

            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Invalid ID. Please enter a valid integer.");
            }
        }
    }
}