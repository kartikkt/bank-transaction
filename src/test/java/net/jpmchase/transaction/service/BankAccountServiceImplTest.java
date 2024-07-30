package net.jpmchase.transaction.service;

import com.google.common.collect.ImmutableList;
import net.jpmchase.transaction.enums.TransactionType;
import net.jpmchase.transaction.model.Transaction;
import net.jpmchase.transaction.service.audit.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BankAccountServiceImplTest {

    private BankAccountService service;
    @Mock
    private AuditService auditService;

    private final long maxBatchValue = 1000L;
    private final long maxBatchSize = 10L;

    @BeforeEach
    public void setUp() {
        auditService = mock(AuditService.class);
        service = new BankAccountServiceImpl(maxBatchValue, maxBatchSize, auditService);
    }

    @Nested
    class PreConditions {

        @Test
        public void injectedServicesMustNotBeNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new BankAccountServiceImpl(null, maxBatchSize, auditService))
                    .withMessage("maxBatchValue must not be null");

            assertThatNullPointerException()
                    .isThrownBy(() -> new BankAccountServiceImpl(maxBatchValue, null, auditService))
                    .withMessage("maxBatchSize must not be null");

            assertThatNullPointerException()
                    .isThrownBy(() -> new BankAccountServiceImpl(maxBatchValue, maxBatchSize, null))
                    .withMessage("auditService must not be null");
            }
    }



    @Nested
    class ProcessTransaction {

        @Test
        public void transactionMustNotBeNull() {
            assertThatNullPointerException()
                    .isThrownBy(() -> service.processTransaction(null))
                    .withMessage("transaction must not be null");
        }

        @Test
        public void processOneTransactionSuccessfully() {
            Transaction transaction = new Transaction("ACC12345", 100.0, TransactionType.CREDIT);
            service.processTransaction(transaction);
            assertEquals(100.0, service.retrieveBalance());
        }

        @Test
        public void processMultipleTransactionSuccessfully() {
            for (int i = 0; i < maxBatchSize; i++) {
                service.processTransaction(new Transaction(UUID.randomUUID().toString(),
                        50.0, TransactionType.CREDIT));
            }
            assertEquals(maxBatchSize * 50.0, service.retrieveBalance());
            verify(auditService, times(1)).publishBatch(any(ImmutableList.class), eq(0));
        }

        @Test
        public void testPublishBatchForAudit_Synchronized() throws InterruptedException {
            Thread t1 = new Thread(() -> service.processTransaction(new Transaction("ACC12345", 100.0,
                    TransactionType.CREDIT)));
            Thread t2 = new Thread(() -> service.processTransaction(new Transaction("ACC12345", 200.0,
                    TransactionType.DEBIT)));
            t1.start();
            t2.start();
            t1.join();
            t2.join();

            assertEquals(service.retrieveBalance(), -100.0);
        }

        @Test
        public void testProcessTransaction_ExceptionHandling() {
            Transaction transaction = mock(Transaction.class);
            doThrow(new RuntimeException("exception")).when(transaction).getAmount();
            assertThatExceptionOfType(RuntimeException.class)
                    .isThrownBy(() -> service.processTransaction(transaction))
                    .withMessage("Error occurred while processing transaction: exception");
        }

    }


    @Nested
    class RetrieveBalance {
        @Test
        public void retrieveCorrectBalance() {
            assertEquals(0.0, service.retrieveBalance());
            service.processTransaction(new Transaction("ACC12345", 100.0, TransactionType.CREDIT));
            assertEquals(100.0, service.retrieveBalance());
        }
    }

}