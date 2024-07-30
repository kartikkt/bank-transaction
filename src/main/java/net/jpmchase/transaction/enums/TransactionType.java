package net.jpmchase.transaction.enums;

public enum TransactionType {
    CREDIT(1),
    DEBIT(-1);

    private final int impact;

    TransactionType(final int impact){
        this.impact= impact;
    }

    public int getImpact(){
        return impact;
    }
}
