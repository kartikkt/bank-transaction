package net.jpmchase.transaction.service.audit;

import net.jpmchase.transaction.enums.TransactionType;
import net.jpmchase.transaction.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AuditServiceImplTest {

    private AuditServiceImpl service;

    @BeforeEach
    public void setUp() {
        service = new AuditServiceImpl();
    }

    @Nested
    class PublishBatch {

        @Test
        public void transactionsMustNotBeNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.publishBatch(null, 0))
                    .withMessage("transactions must not be null");
        }

        @Test
        public void transactionsMustNotContainNullElements() {
            List<Transaction> txns = new ArrayList<>();
            txns.add(null);
            txns.add(new Transaction("A", 1.0, TransactionType.CREDIT));
            assertThatExceptionOfType(IllegalArgumentException.class)
                    .isThrownBy(() -> service.publishBatch( txns, 0))
                    .withMessage("transactions must have no null elements");
        }

        @Test
        public void batchesArePublishedProperly() {
            final List<Transaction> transactions = List.of(
                    new Transaction("ACC12345", 100.0, TransactionType.CREDIT),
                    new Transaction("ACC67890", 200.0, TransactionType.CREDIT)
            );

            service.publishBatch(transactions, 1);
            assertEquals(service.getTransactionHistory().size(), transactions.size());
        }

        @Test
        public void testPublishBatchSynchronized() throws InterruptedException {
            List<Transaction> transactions = List.of(
                    new Transaction("ACC12345", 100.0, TransactionType.CREDIT),
                    new Transaction("ACC67890", 200.0, TransactionType.DEBIT)
            );

            Thread t1 = new Thread(() -> service.publishBatch(transactions, 1));
            Thread t2 = new Thread(() -> service.publishBatch(transactions, 2));
            t1.start();
            t2.start();
            t1.join();
            t2.join();

            assertEquals(4, service.getTransactionHistory().size());
        }

    }
}
