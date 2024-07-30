package net.jpmchase.transaction.producer;

import net.jpmchase.transaction.model.Transaction;
import net.jpmchase.transaction.service.BankAccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionProducerTest {

    private TransactionProducer transactionProducer;
    @Mock
    private BankAccountService bankAccountService;

    @BeforeEach
    public void setUp() {
        transactionProducer = new TransactionProducer(bankAccountService, 20000L, 500000L);
    }

    @Test
    public void testStartProducing() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(10);
        executorService.execute(transactionProducer::startProducing);
        Thread.sleep(100);
        executorService.shutdownNow();
        verify(bankAccountService, atLeastOnce()).processTransaction(any(Transaction.class));
    }
}
