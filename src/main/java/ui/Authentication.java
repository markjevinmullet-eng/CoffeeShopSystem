package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.Scanner;

public class Authentication {
    private final UserDao userDao;
    private final Scanner scanner;

    public Authentication(UserDao userDao, Scanner scanner) {
        this.userDao = userDao;
        this.scanner = scanner;
    }

    public void start() {
        boolean exit = false;
        while (!exit) {
            System.out.println("\n=============================================");
            System.out.println(" COFFEE SHOP INVENTORY AND ORDERING SYSTEM ");
            System.out.println("=============================================");
            System.out.println("1. Login");
            System.out.println("2. Register New Account");
            System.out.println("3. Logout / Exit");
            System.out.print("Choose an option (1-3): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> handleLogin();
                case "2" -> handleRegister();
                case "3" -> {
                    System.out.println("\nExiting application. Goodbye!");
                    exit = true;
                }
                default -> System.out.println("❌ Invalid choice. Please enter 1, 2, or 3.");
            }
        }
    }

    private void handleLogin() {
        System.out.println("\n--- USER LOGIN ---");
        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        User user = userDao.login(username, password);
        if (user != null) {
            if ("PENDING".equalsIgnoreCase(user.status())) {
                System.out.println("❌ Account Activation Required: Your account status is PENDING approval.");
            } else {
                System.out.println("\n✅ Login successful!");
                System.out.println("Welcome, " + user.username() + "! Role: " + user.roles() + " | Status: " + user.status());

                // Route user based on their role
                String role = user.roles().toUpperCase();
                switch (role) {
                    case "MANAGER" -> {
                        AdminMenu adminMenu = new AdminMenu(userDao, scanner, user);
                        adminMenu.start();
                    }
                    case "CASHIER" -> {
                        CashierMenu cashierMenu = new CashierMenu(scanner, user);
                        cashierMenu.start();
                    }
                    case "CUSTOMER" -> {
                        CustomerMenu customerMenu = new CustomerMenu(scanner, user);
                        customerMenu.start();
                    }
                    default -> System.out.println("❌ Unrecognized role menu.");
                }
            }
        } else {
            System.out.println("❌ Invalid username or password.");
        }
    }

    private void handleRegister() {
        System.out.println("\n--- REGISTER ACCOUNT ---");
        System.out.print("Enter Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter Password: ");
        String password = scanner.nextLine().trim();

        System.out.println("Select Role:");
        System.out.println("1. MANAGER");
        System.out.println("2. CASHIER");
        System.out.println("3. CUSTOMER");
        System.out.print("Choose role (1-3): ");

        String roleChoice = scanner.nextLine().trim();
        String role = switch (roleChoice) {
            case "1" -> "MANAGER";
            case "2" -> "CASHIER";
            default -> "CUSTOMER";
        };

        boolean success = userDao.registerUser(username, password, role);
        if (success) {
            System.out.println("✅ Account registered successfully! Status is set to PENDING.");
        } else {
            System.out.println("❌ Registration failed. Username may already exist.");
        }
    }
}