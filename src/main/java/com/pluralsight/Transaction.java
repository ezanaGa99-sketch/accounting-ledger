package com.pluralsight;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Transaction {

    // Keeps the time looking like 09:40:18 (with seconds) when we print or save it
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    // The 5 parts of one transaction (one line in transactions.csv)
    private LocalDate date;
    private LocalTime time;
    private String description;
    private String vendor;
    private double amount;

    public Transaction(LocalDate date, LocalTime time, String description, String vendor, double amount) {
        this.date = date;
        this.time = time;
        this.description = description;
        this.vendor = vendor;
        this.amount = amount;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    public String getVendor() {
        return vendor;
    }

    public double getAmount() {
        return amount;
    }

    // A deposit is money coming in (positive amount)
    public boolean isDeposit() {
        return amount > 0;
    }

    // A payment is money going out (negative amount)
    public boolean isPayment() {
        return amount < 0;
    }

    // Turns this transaction back into one line for the CSV file
    public String toCsvLine() {
        return date + "|" + time.format(TIME_FORMAT) + "|" + description + "|" + vendor + "|"
                + String.format("%.2f", amount);
    }

    // How a transaction looks when we print it on the screen
    @Override
    public String toString() {
        return String.format("%-12s %-10s %-28s %-18s %10.2f",
                date, time.format(TIME_FORMAT), description, vendor, amount);
    }
}