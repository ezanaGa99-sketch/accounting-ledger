package com.pluralsight;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Scanner;

public class LedgerApp {

    static Scanner scanner = new Scanner(System.in);
    static ArrayList<Transaction> transactions = new ArrayList<>();

    public static void main(String[] args) {

        // Load all transactions from the file when the app starts
        transactions = TransactionFileManager.getTransactionsFromFile();

        boolean running = true;

        // HOME SCREEN: keeps running until the user chooses X
        while (running) {
            System.out.println();
            System.out.println("===== Accounting Ledger =====");
            System.out.println("D) Add Deposit");
            System.out.println("P) Make Payment (Debit)");
            System.out.println("L) Ledger");
            System.out.println("X) Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "D":
                    addTransaction(true);
                    break;
                case "P":
                    addTransaction(false);
                    break;
                case "L":
                    System.out.println("Ledger is coming soon!");
                    break;
                case "X":
                    System.out.println("Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }

    // Asks for the details of a deposit or payment and saves it
    // isDeposit = true for a deposit, false for a payment
    public static void addTransaction(boolean isDeposit) {
        System.out.println();
        System.out.println(isDeposit ? "===== Add Deposit =====" : "===== Make Payment =====");

        String description = readText("Description: ");
        String vendor = readText("Vendor: ");
        double amount = readPositiveAmount("Amount: $");

        // Payments are stored as negative numbers
        if (!isDeposit) {
            amount = -amount;
        }

        // Date and time are filled in automatically (now), without nanoseconds
        Transaction transaction = new Transaction(LocalDate.now(), LocalTime.now().withNano(0),
                description, vendor, amount);

        transactions.add(transaction);
        TransactionFileManager.saveTransaction(transaction);

        System.out.println(isDeposit ? "Deposit saved!" : "Payment saved!");
    }

    // Keeps asking until the user types something (and removes any | so the CSV doesn't break)
    public static String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = scanner.nextLine().trim().replace("|", "-");
            if (!text.isEmpty()) {
                return text;
            }
            System.out.println("This can't be empty, please try again.");
        }
    }

    // Keeps asking until the user types a number greater than 0
    public static double readPositiveAmount(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                double amount = Double.parseDouble(scanner.nextLine().trim());
                if (amount > 0) {
                    return amount;
                }
                System.out.println("The amount must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number, like 50 or 19.99");
            }
        }
    }
}