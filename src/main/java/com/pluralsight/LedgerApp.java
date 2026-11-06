package com.pluralsight;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
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
                    ledgerScreen();
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

    // ===================== LEDGER SCREEN =====================

    public static void ledgerScreen() {
        boolean onThisScreen = true;

        while (onThisScreen) {
            System.out.println();
            System.out.println("===== Ledger =====");
            System.out.println("A) All entries");
            System.out.println("D) Deposits");
            System.out.println("P) Payments");
            System.out.println("R) Reports");
            System.out.println("H) Home");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim().toUpperCase();

            switch (choice) {
                case "A":
                    printTransactions("All Entries", getNewestFirst());
                    break;
                case "D":
                    ArrayList<Transaction> deposits = new ArrayList<>();
                    for (Transaction t : getNewestFirst()) {
                        if (t.isDeposit()) {
                            deposits.add(t);
                        }
                    }
                    printTransactions("Deposits", deposits);
                    break;
                case "P":
                    ArrayList<Transaction> payments = new ArrayList<>();
                    for (Transaction t : getNewestFirst()) {
                        if (t.isPayment()) {
                            payments.add(t);
                        }
                    }
                    printTransactions("Payments", payments);
                    break;
                case "R":
                    reportsScreen();
                    break;
                case "H":
                    onThisScreen = false;
                    break;
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }

    // ===================== REPORTS SCREEN =====================

    public static void reportsScreen() {
        boolean onThisScreen = true;

        while (onThisScreen) {
            System.out.println();
            System.out.println("===== Reports =====");
            System.out.println("1) Month To Date");
            System.out.println("2) Previous Month");
            System.out.println("3) Year To Date");
            System.out.println("4) Previous Year");
            System.out.println("5) Search by Vendor");
            System.out.println("0) Back");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            LocalDate today = LocalDate.now();

            switch (choice) {
                case "1":
                    // From the 1st of this month until today
                    LocalDate monthStart = today.withDayOfMonth(1);
                    printTransactions("Month To Date", filterByDate(monthStart, today));
                    break;
                case "2":
                    // From the 1st to the last day of last month
                    LocalDate prevMonthStart = today.minusMonths(1).withDayOfMonth(1);
                    LocalDate prevMonthEnd = prevMonthStart.withDayOfMonth(prevMonthStart.lengthOfMonth());
                    printTransactions("Previous Month", filterByDate(prevMonthStart, prevMonthEnd));
                    break;
                case "3":
                    // From January 1st of this year until today
                    LocalDate yearStart = today.withDayOfYear(1);
                    printTransactions("Year To Date", filterByDate(yearStart, today));
                    break;
                case "4":
                    // From January 1st to December 31st of last year
                    LocalDate prevYearStart = today.minusYears(1).withDayOfYear(1);
                    LocalDate prevYearEnd = prevYearStart.withDayOfYear(prevYearStart.lengthOfYear());
                    printTransactions("Previous Year", filterByDate(prevYearStart, prevYearEnd));
                    break;
                case "5":
                    searchByVendor();
                    break;
                case "0":
                    onThisScreen = false;
                    break;
                default:
                    System.out.println("Invalid option, please try again.");
            }
        }
    }

    // Returns the transactions between start and end (both included), newest first
    public static ArrayList<Transaction> filterByDate(LocalDate start, LocalDate end) {
        ArrayList<Transaction> results = new ArrayList<>();
        for (Transaction t : getNewestFirst()) {
            if (!t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
                results.add(t);
            }
        }
        return results;
    }

    // Asks for a vendor name and shows all transactions for that vendor
    public static void searchByVendor() {
        String vendor = readText("Enter the vendor name: ");

        ArrayList<Transaction> results = new ArrayList<>();
        for (Transaction t : getNewestFirst()) {
            if (t.getVendor().equalsIgnoreCase(vendor)) {
                results.add(t);
            }
        }
        printTransactions("Vendor: " + vendor, results);
    }

    // ===================== SHARED HELPERS =====================

    // Returns a COPY of all transactions, sorted newest first (by date, then time)
    public static ArrayList<Transaction> getNewestFirst() {
        ArrayList<Transaction> sorted = new ArrayList<>(transactions);
        sorted.sort(Comparator.comparing(Transaction::getDate)
                .thenComparing(Transaction::getTime)
                .reversed());
        return sorted;
    }

    // Prints a list of transactions with a title, column headers, the count, and the total
    public static void printTransactions(String title, ArrayList<Transaction> list) {
        System.out.println();
        System.out.println("===== " + title + " =====");
        if (list.isEmpty()) {
            System.out.println("No transactions found.");
            return;
        }
        System.out.printf("%-12s %-10s %-28s %-18s %10s%n",
                "Date", "Time", "Description", "Vendor", "Amount");
        System.out.println("-------------------------------------------------------------------------------------");

        double total = 0;
        for (Transaction t : list) {
            System.out.println(t);
            total += t.getAmount();
        }

        System.out.println("-------------------------------------------------------------------------------------");
        System.out.printf("%d transaction(s)   Total: $%.2f%n", list.size(), total);
    }

    // ===================== ADD DEPOSIT / PAYMENT =====================

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

    // ===================== INPUT HELPERS =====================

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