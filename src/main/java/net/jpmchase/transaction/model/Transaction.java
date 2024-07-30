package net.jpmchase.transaction.model;

import net.jpmchase.transaction.enums.TransactionType;

import static org.apache.commons.lang3.Validate.notNull;


public class Transaction {
    private final String id;
    private final double amount;
    private final TransactionType transactionType;

    public Transaction(final String id,
                       final double amount,
                       final TransactionType transactionType) {
        this.id = notNull(id, "id must not be null");
        this.amount = amount;
        this.transactionType = notNull(transactionType, "transactionType must not be null");
    }

    public String getId() {
        return id;
    }

    public double getAmount() {
        return amount;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

}
