package com.pluralsight;

import java.util.ArrayList;

public class LedgerApp {

    public static void main(String[] args) {

        // Quick test: load the file and print every transaction
        ArrayList<Transaction> transactions = TransactionFileManager.getTransactionsFromFile();

        System.out.println("Loaded " + transactions.size() + " transactions:");
        for (Transaction t : transactions) {
            System.out.println(t);
        }
    }
}
