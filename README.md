# ClaimWise Recovery Ledger

A Java accounting ledger for tracking money in and money out, saved to a CSV file so nothing is lost when the app closes.

It comes in two versions:
- **LedgerApp** – the command-line app (menus in the console)
- **LedgerWindow** – a window app with buttons and a color-coded table (bonus)

## About the Theme

ClaimWise is an **unclaimed property recovery** business. States hold billions of dollars in money that companies never collected (uncashed checks, old deposits, refunds). A finder locates the owner, files the claim, and earns a fee, usually 10% of what is recovered.

- **Deposits** = finder fees paid out when a state releases a claim
- **Payments** = business costs (legal review, state registration fees, data tools, mailing, taxes)

## Features

**Home Screen**
- **D) Add Deposit** – enter a description, vendor, and amount; date and time are added automatically
- **P) Make Payment** – same as a deposit, but saved as a negative amount
- **L) Ledger** – view transactions
- **X) Exit**

**Ledger Screen** (newest entries first)
- **A) All** – every transaction
- **D) Deposits** – only money coming in
- **P) Payments** – only money going out
- **R) Reports** – pre-built reports
- **H) Home**

**Reports Screen**
- **1)** Month To Date
- **2)** Previous Month
- **3)** Year To Date
- **4)** Previous Year
- **5)** Search by Vendor
- **0)** Back

Every list shows the number of transactions and the total amount.

## Bonus: Window App (LedgerWindow)

A GUI version built with **Java Swing** that opens in its own window:
- **+ Finder Fee** and **+ Expense** buttons open a form to add a transaction
- **All**, **Deposits**, and **Payments** buttons filter the table
- **Reports** dropdown: Month To Date, Previous Month, Year To Date, Previous Year
- **Search Vendor** button
- Deposits show in **green**, payments in **red**, with the count and total at the bottom

## How to Run

1. Clone this repo and open it in IntelliJ
2. **Command-line app:** run `LedgerApp.java` (uses `transactions.csv`)
3. **Window app:** run `LedgerWindow.java` (uses `claimwise.csv`)

Both CSV files must be in the project root (next to `pom.xml`).

## CSV Format

```
date|time|description|vendor|amount
2026-11-03|11:15:00|Fee: Keystone Escrow Svcs|PA Treasury|1050.00
2026-11-04|12:42:22|Certified mail - claim docs|USPS|-36.80
```

Positive amounts are deposits; negative amounts are payments.

## Project Structure

| File                          | What it does                                                    |
|-------------------------------|-----------------------------------------------------------------|
| `Transaction.java`            | Holds one transaction (date, time, description, vendor, amount) |
| `TransactionFileManager.java` | Reads and saves `transactions.csv`                              |
| `LedgerApp.java`              | Command-line app: Home, Ledger, and Reports screens             |
| `LedgerWindow.java`           | Window app (Swing) that reads and saves `claimwise.csv`         |

## Example Output (Command Line)

```
===== Previous Month =====
Date         Time       Description                  Vendor                 Amount
-------------------------------------------------------------------------------------
2026-10-28   18:22:05   Groceries                    Costco                -156.32
2026-10-15   12:00:00   Paycheck                     Year Up United        1350.00
2026-10-03   08:15:42   Ergonomic keyboard           Amazon                 -89.50
-------------------------------------------------------------------------------------
3 transaction(s)   Total: $1104.18
```

## Interesting Piece of Code

All the date reports share one method, `filterByDate`. Each report just works out a start and end date and passes them in:

```java
public static ArrayList<Transaction> filterByDate(LocalDate start, LocalDate end) {
    ArrayList<Transaction> results = new ArrayList<>();
    for (Transaction t : getNewestFirst()) {
        if (!t.getDate().isBefore(start) && !t.getDate().isAfter(end)) {
            results.add(t);
        }
    }
    return results;
}
```

For example, Previous Month uses `lengthOfMonth()` to find the last day, so it works for 28, 30, or 31-day months without any extra code. The window app reuses the same idea, so both versions give the same report results.