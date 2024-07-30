package net.jpmchase.transaction.service.audit;

import com.google.common.collect.ImmutableList;
import net.jpmchase.transaction.model.Transaction;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.apache.commons.lang3.Validate.noNullElements;
import static org.apache.commons.lang3.Validate.notNull;

@Service
public class AuditServiceImpl implements AuditService {

    private final ImmutableList.Builder<Transaction> transactionHistory = ImmutableList.builder();
    public synchronized void publishBatch(final List<Transaction> transactions,
                                          final int batchNumber) {
        notNull(transactions, "transactions must not be null");
        noNullElements(transactions, "transactions must have no null elements");

        double totalValue = transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();

        System.out.println("Batch Number: " + batchNumber);
        System.out.println("Total value: £" + totalValue);
        System.out.println("Count of transactions: " + transactions.size());

        transactionHistory.addAll(transactions);
        // Send transactions to downstream system
    }

    public ImmutableList<Transaction> getTransactionHistory(){
        return transactionHistory.build();
    }
}
