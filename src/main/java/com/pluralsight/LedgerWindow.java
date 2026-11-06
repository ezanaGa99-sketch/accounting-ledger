package com.pluralsight;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;

// BONUS: a window (GUI) version of the ledger, built with Java Swing.
// It uses the same Transaction class as LedgerApp, but its OWN data file (claimwise.csv),
// so the text app's transactions.csv is never changed by the window.
public class LedgerWindow {

    static final String APP_NAME = "ClaimWise Recovery Ledger";
    static final String DATA_FILE = "claimwise.csv";

    static final Color NAVY = new Color(20, 40, 75);
    static final Color GREEN = new Color(0, 140, 70);
    static final Color RED = new Color(200, 40, 40);

    // Shows times like 12:00:00 (with seconds)
    static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    static ArrayList<Transaction> transactions = new ArrayList<>();

    // The parts of the window we need to change later
    static JFrame frame;
    static DefaultTableModel tableModel;
    static JLabel titleLabel;
    static JLabel summaryLabel;

    public static void main(String[] args) {
        transactions = loadTransactions();

        // Swing windows should be built on Swing's own thread
        SwingUtilities.invokeLater(() -> buildWindow());
    }

    // ===================== BUILD THE WINDOW =====================

    public static void buildWindow() {
        frame = new JFrame(APP_NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 620);
        frame.setLayout(new BorderLayout());

        // ---- Top: banner + buttons ----
        JPanel top = new JPanel(new BorderLayout());

        JPanel banner = new JPanel(new GridLayout(2, 1));
        banner.setBackground(NAVY);
        banner.setBorder(BorderFactory.createEmptyBorder(14, 20, 14, 20));
        JLabel name = new JLabel(APP_NAME);
        name.setFont(new Font("SansSerif", Font.BOLD, 26));
        name.setForeground(Color.WHITE);
        JLabel tagline = new JLabel("Unclaimed property finder fees & business costs");
        tagline.setFont(new Font("SansSerif", Font.PLAIN, 14));
        tagline.setForeground(new Color(190, 205, 230));
        banner.add(name);
        banner.add(tagline);
        top.add(banner, BorderLayout.NORTH);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 10));

        JButton addFee = new JButton("+ Finder Fee");
        addFee.addActionListener(e -> addTransaction(true));
        JButton addExpense = new JButton("+ Expense");
        addExpense.addActionListener(e -> addTransaction(false));

        JButton all = new JButton("All");
        all.addActionListener(e -> showTransactions("All Entries", getNewestFirst()));
        JButton deposits = new JButton("Deposits");
        deposits.addActionListener(e -> showDeposits());
        JButton payments = new JButton("Payments");
        payments.addActionListener(e -> showPayments());

        String[] reports = {"Reports...", "Month To Date", "Previous Month",
                "Year To Date", "Previous Year"};
        JComboBox<String> reportBox = new JComboBox<>(reports);
        reportBox.addActionListener(e -> {
            runReport((String) reportBox.getSelectedItem());
            reportBox.setSelectedIndex(0); // go back to "Reports..."
        });

        JButton searchVendor = new JButton("Search Vendor");
        searchVendor.addActionListener(e -> searchByVendor());

        buttons.add(addFee);
        buttons.add(addExpense);
        buttons.add(new JSeparator(SwingConstants.VERTICAL));
        buttons.add(all);
        buttons.add(deposits);
        buttons.add(payments);
        buttons.add(reportBox);
        buttons.add(searchVendor);
        top.add(buttons, BorderLayout.SOUTH);

        frame.add(top, BorderLayout.NORTH);

        // ---- Middle: title + table ----
        JPanel middle = new JPanel(new BorderLayout());
        middle.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));

        titleLabel = new JLabel();
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(4, 2, 8, 0));
        middle.add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Date", "Time", "Description", "Vendor", "Amount"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // the table is read-only
            }
        };
        JTable table = new JTable(tableModel);
        table.setRowHeight(26);
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));
        table.getColumnModel().getColumn(2).setPreferredWidth(300);
        table.getColumnModel().getColumn(3).setPreferredWidth(220);
        table.getColumnModel().getColumn(4).setCellRenderer(new AmountRenderer());
        middle.add(new JScrollPane(table), BorderLayout.CENTER);

        frame.add(middle, BorderLayout.CENTER);

        // ---- Bottom: count + total ----
        summaryLabel = new JLabel();
        summaryLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        summaryLabel.setBorder(BorderFactory.createEmptyBorder(10, 14, 12, 14));
        frame.add(summaryLabel, BorderLayout.SOUTH);

        // Start by showing everything, newest first
        showTransactions("All Entries", getNewestFirst());

        frame.setLocationRelativeTo(null); // center on the screen
        frame.setVisible(true);
    }

    // ===================== SHOW TRANSACTIONS =====================

    // Fills the table with a list, and updates the title, count and total
    public static void showTransactions(String title, ArrayList<Transaction> list) {
        titleLabel.setText(title);
        tableModel.setRowCount(0); // clear the table

        double total = 0;
        for (Transaction t : list) {
            tableModel.addRow(new Object[]{t.getDate(), t.getTime().format(TIME_FORMAT),
                    t.getDescription(), t.getVendor(), t.getAmount()});
            total += t.getAmount();
        }

        summaryLabel.setText(String.format("%d transaction(s)     Total: $%,.2f", list.size(), total));
        summaryLabel.setForeground(total >= 0 ? GREEN : RED);
    }

    public static void showDeposits() {
        ArrayList<Transaction> deposits = new ArrayList<>();
        for (Transaction t : getNewestFirst()) {
            if (t.isDeposit()) {
                deposits.add(t);
            }
        }
        showTransactions("Deposits (Finder Fees)", deposits);
    }

    public static void showPayments() {
        ArrayList<Transaction> payments = new ArrayList<>();
        for (Transaction t : getNewestFirst()) {
            if (t.isPayment()) {
                payments.add(t);
            }
        }
        showTransactions("Payments (Expenses)", payments);
    }

    // ===================== REPORTS =====================

    public static void runReport(String report) {
        LocalDate today = LocalDate.now();

        switch (report) {
            case "Month To Date":
                showTransactions(report, filterByDate(today.withDayOfMonth(1), today));
                break;
            case "Previous Month":
                LocalDate prevMonthStart = today.minusMonths(1).withDayOfMonth(1);
                LocalDate prevMonthEnd = prevMonthStart.withDayOfMonth(prevMonthStart.lengthOfMonth());
                showTransactions(report, filterByDate(prevMonthStart, prevMonthEnd));
                break;
            case "Year To Date":
                showTransactions(report, filterByDate(today.withDayOfYear(1), today));
                break;
            case "Previous Year":
                LocalDate prevYearStart = today.minusYears(1).withDayOfYear(1);
                LocalDate prevYearEnd = prevYearStart.withDayOfYear(prevYearStart.lengthOfYear());
                showTransactions(report, filterByDate(prevYearStart, prevYearEnd));
                break;
            default:
                // "Reports..." was picked, so do nothing
        }
    }

    public static void searchByVendor() {
        String vendor = JOptionPane.showInputDialog(frame, "Vendor name:", "Search by Vendor",
                JOptionPane.QUESTION_MESSAGE);
        if (vendor == null || vendor.isBlank()) {
            return; // the user clicked Cancel or typed nothing
        }

        ArrayList<Transaction> results = new ArrayList<>();
        for (Transaction t : getNewestFirst()) {
            if (t.getVendor().equalsIgnoreCase(vendor.trim())) {
                results.add(t);
            }
        }
        showTransactions("Vendor: " + vendor.trim(), results);
    }

    // ===================== ADD A TRANSACTION =====================

    // Opens a small form. isDeposit = true for a finder fee, false for an expense
    public static void addTransaction(boolean isDeposit) {
        JTextField descriptionField = new JTextField(20);
        JTextField vendorField = new JTextField(20);
        JTextField amountField = new JTextField(10);

        JPanel form = new JPanel(new GridLayout(3, 2, 6, 6));
        form.add(new JLabel("Description:"));
        form.add(descriptionField);
        form.add(new JLabel(isDeposit ? "Paid by (state):" : "Paid to (vendor):"));
        form.add(vendorField);
        form.add(new JLabel("Amount ($):"));
        form.add(amountField);

        String title = isDeposit ? "Record Finder Fee" : "Record Expense";
        int choice = JOptionPane.showConfirmDialog(frame, form, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (choice != JOptionPane.OK_OPTION) {
            return; // the user clicked Cancel
        }

        // Remove any | so the CSV doesn't break
        String description = descriptionField.getText().trim().replace("|", "-");
        String vendor = vendorField.getText().trim().replace("|", "-");

        if (description.isEmpty() || vendor.isEmpty()) {
            showError("Description and vendor can't be empty.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountField.getText().trim());
        } catch (NumberFormatException e) {
            showError("Please enter a number, like 50 or 19.99");
            return;
        }
        if (amount <= 0) {
            showError("The amount must be greater than 0.");
            return;
        }

        // Expenses are stored as negative numbers
        if (!isDeposit) {
            amount = -amount;
        }

        Transaction transaction = new Transaction(LocalDate.now(), LocalTime.now().withNano(0),
                description, vendor, amount);
        transactions.add(transaction);
        saveTransaction(transaction);

        showTransactions("All Entries", getNewestFirst());
        JOptionPane.showMessageDialog(frame, isDeposit ? "Finder fee saved!" : "Expense saved!");
    }

    public static void showError(String message) {
        JOptionPane.showMessageDialog(frame, message, "Oops", JOptionPane.ERROR_MESSAGE);
    }

    // ===================== FILE: claimwise.csv =====================

    // Reads claimwise.csv and returns a list of Transaction objects
    public static ArrayList<Transaction> loadTransactions() {
        ArrayList<Transaction> list = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(DATA_FILE));
            String line;

            br.readLine(); // skip the header line

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue; // skip empty lines
                }
                String[] parts = line.split("\\|");
                list.add(new Transaction(LocalDate.parse(parts[0]), LocalTime.parse(parts[1]),
                        parts[2], parts[3], Double.parseDouble(parts[4])));
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Oh oh: " + e.getMessage());
        }
        return list;
    }

    // Adds one transaction to the END of claimwise.csv
    public static void saveTransaction(Transaction transaction) {
        try {
            // true = append mode (add to the end, don't erase the file)
            BufferedWriter bw = new BufferedWriter(new FileWriter(DATA_FILE, true));
            bw.write(transaction.toCsvLine());
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            System.out.println("Oh oh: " + e.getMessage());
        }
    }

    // ===================== HELPERS =====================

    // Returns a COPY of all transactions, sorted newest first (by date, then time)
    public static ArrayList<Transaction> getNewestFirst() {
        ArrayList<Transaction> sorted = new ArrayList<>(transactions);
        sorted.sort(Comparator.comparing(Transaction::getDate)
                .thenComparing(Transaction::getTime)
                .reversed());
        return sorted;
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

    // Shows the Amount column as $1,500.00 in green, or -$89.50 in red
    static class AmountRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            double amount = (Double) value;
            setText(String.format(amount < 0 ? "-$%,.2f" : "$%,.2f", Math.abs(amount)));
            setHorizontalAlignment(SwingConstants.RIGHT);
            if (!isSelected) {
                setForeground(amount < 0 ? RED : GREEN);
            }
            return this;
        }
    }
}