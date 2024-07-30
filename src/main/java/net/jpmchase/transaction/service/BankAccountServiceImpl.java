package net.jpmchase.transaction.service;


import com.google.common.collect.ImmutableList;
import net.jpmchase.transaction.model.Transaction;
import net.jpmchase.transaction.service.audit.AuditService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;

import static org.apache.commons.lang3.Validate.notNull;


@Service
public class BankAccountServiceImpl implements BankAccountService {
    private static final Logger logger = LogManager.getLogger(BankAccountServiceImpl.class);
    private final DoubleAdder balance = new DoubleAdder();
    private final List<Transaction> transactions = Collections.synchronizedList(new ArrayList<>());
    private final double maxBatchValue;
    private final double maxBatchSize;
    private final AuditService auditService;
    private int batchNumber = 0;

    public BankAccountServiceImpl(@Value("${audit.batch-capacity}") final Long maxBatchValue,
                                  @Value("${audit.batch-capacity}") final Long maxBatchSize,
                                  final AuditService auditService ) {
        this.maxBatchValue = notNull(maxBatchValue, "maxBatchValue must not be null");
        this.maxBatchSize = notNull(maxBatchSize, "maxBatchSize must not be null");
        this.auditService = notNull(auditService, "auditService must not be null");
    }

    @Override
    public synchronized void processTransaction(final Transaction transaction) {
        notNull(transaction, "transaction must not be null");

        try {
            transactions.add(transaction);
            balance.add(transaction.getAmount() * transaction.getTransactionType().getImpact());

            if (shouldPublishBatch()) publishBatchForAudit();

        } catch (final Exception e) {
            logger.error("Error processing transaction: {}", transaction, e);
            throw new RuntimeException("Error occurred while processing transaction: "+ e.getMessage());
        }
    }

    @Override
    public synchronized double retrieveBalance() {
        return balance.sum();
    }

    private boolean shouldPublishBatch() {
        double totalValue = transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .sum();
        return totalValue >= maxBatchValue || transactions.size() >= maxBatchSize;
    }

    private void publishBatchForAudit() {
        final ImmutableList<Transaction> batchForAudit;
        synchronized (transactions) {
            batchForAudit = ImmutableList.copyOf(transactions);
            transactions.clear();
        }
        auditService.publishBatch(batchForAudit, batchNumber);
        batchNumber++;
    }
}
