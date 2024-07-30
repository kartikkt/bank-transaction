package net.jpmchase.transaction.service.audit;

import net.jpmchase.transaction.model.Transaction;

import java.util.List;

public interface AuditService {

    void publishBatch(final List<Transaction> transactions, final int batchNumber);
}
