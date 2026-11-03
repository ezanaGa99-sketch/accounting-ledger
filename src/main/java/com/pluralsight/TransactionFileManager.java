package com.pluralsight;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;

public class TransactionFileManager {

    private static final String FILE_NAME = "transactions.csv";

    // Reads transactions.csv and returns a list of Transaction objects
    public static ArrayList<Transaction> getTransactionsFromFile() {
        ArrayList<Transaction> transactions = new ArrayList<Transaction>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILE_NAME));
            String line;

            br.readLine(); // skip the header line

            while ((line = br.readLine()) != null) {
                if (line.isBlank()) {
                    continue; // skip empty lines
                }

                String[] parts = line.split("\\|");

                LocalDate date = LocalDate.parse(parts[0]);
                LocalTime time = LocalTime.parse(parts[1]);
                String description = parts[2];
                String vendor = parts[3];
                double amount = Double.parseDouble(parts[4]);

                transactions.add(new Transaction(date, time, description, vendor, amount));
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Oh oh: " + e.getMessage());
        }
        return transactions;
    }
}
