package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.List;
import java.util.Scanner;

public class AdminMenu {
    private final UserDao userDao;
    private final Scanner scanner;
    private final User adminUser;

    public AdminMenu(UserDao userDao, Scanner scanner, User adminUser) {
        this.userDao = userDao;
        this.scanner = scanner;
        this.adminUser = adminUser;
    }

    public void start() {
        boolean back = false;
        while (!back) {
            System.out.println("\n=============================================");
            System.out.println("       MANAGER / ADMIN MANAGEMENT PANEL      ");
            System.out.println("=============================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout / Back to Main");
            System.out.print("Choose an option (1-4): ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllUsers();
                case "2" -> viewPendingUsers();
                case "3" -> approveUserWorkflow();
                case "4" -> {
                    System.out.println("Logging out from Admin panel...");
                    back = true;
                }
                default -> System.out.println("❌ Invalid choice. Please enter 1-4.");
            }
        }
    }

    private void displayUserTable(List<User> users) {
        if (users.isEmpty()) {
            System.out.println("\n[No user records found.]");
            return;
        }

        // Formatted table header using professor's formatters (%-5s, %-15s, %-12s, %-10s)
        String format = "%-5s | %-15s | %-12s | %-10s%n";
        System.out.println("\n---------------------------------------------------------");
        System.out.printf(format, "ID", "USERNAME", "ROLE", "STATUS");
        System.out.println("---------------------------------------------------------");

        for (User u : users) {
            System.out.printf(format, u.id(), u.username(), u.roles(), u.status());
        }
        System.out.println("---------------------------------------------------------");
    }

    private void viewAllUsers() {
        System.out.println("\n--- ALL SYSTEM USERS ---");
        List<User> users = userDao.getAllUsers();
        displayUserTable(users);
    }

    private void viewPendingUsers() {
        System.out.println("\n--- PENDING REGISTRATIONS ---");
        List<User> pending = userDao.getPendingUsers();
        displayUserTable(pending);
    }

    private void approveUserWorkflow() {
        System.out.println("\n--- APPROVE USER ACCOUNT ---");
        List<User> pending = userDao.getPendingUsers();

        if (pending.isEmpty()) {
            System.out.println("✅ There are currently no pending accounts to approve.");
            return;
        }

        displayUserTable(pending);

        System.out.print("\nEnter User ID to approve (or enter 0 to cancel): ");
        String input = scanner.nextLine().trim();

        int targetId;
        try {
            targetId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("❌ Invalid ID format. Approval action cancelled.");
            return;
        }

        // Input safety cancellation check
        if (targetId == 0) {
            System.out.println("Action cancelled by user.");
            return;
        }

        // Verify if ID exists in pending list
        boolean existsInPending = pending.stream().anyMatch(u -> u.id() == targetId);

        if (!existsInPending) {
            System.out.println("❌ Error: User ID " + targetId + " was not found in the pending list.");
            return;
        }

        boolean success = userDao.approveUser(targetId);
        if (success) {
            System.out.println("✅ Success! User ID " + targetId + " status updated to 'APPROVED'.");
        } else {
            System.out.println("❌ Failed to update user status in the database.");
        }
    }
}