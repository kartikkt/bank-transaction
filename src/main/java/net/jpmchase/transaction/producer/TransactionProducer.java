package net.jpmchase.transaction.producer;

import jakarta.annotation.PostConstruct;
import net.jpmchase.transaction.enums.TransactionType;
import net.jpmchase.transaction.model.Transaction;
import net.jpmchase.transaction.service.BankAccountService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;

@Component
public class TransactionProducer {
    private static final Logger logger = LogManager.getLogger(TransactionProducer.class);
    private final BankAccountService bankAccountService;
    private final Long lowerLimit;
    private final Long upperLimit;

    public TransactionProducer(final BankAccountService bankAccountService,
                               @Value("${transaction-producer.amount-lower-limit}") final Long lowerLimit,
                               @Value("${transaction-producer.amount-lower-limit}") final Long upperLimit) {
        this.bankAccountService = bankAccountService;
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
    }

    @PostConstruct
    public void startProducing() {
        Executors.newFixedThreadPool(10).execute(this::produceCredits);
        Executors.newFixedThreadPool(10).execute(this::produceDebits);
    }

    private void produceCredits() {
        while(true) {
            Transaction credit = generateRandomTxn(TransactionType.CREDIT);
            bankAccountService.processTransaction(credit);
            try {
                Thread.sleep(40);  // 25 credits per second
            } catch (final InterruptedException e) {
                logger.error("Error in credit producer thread", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private void produceDebits() {
        while(true) {
            Transaction debit = generateRandomTxn(TransactionType.DEBIT);
            bankAccountService.processTransaction(debit);
            try {
                Thread.sleep(40);  // 25 debits per second
            } catch (InterruptedException e) {
                logger.error("Error in debit producer thread", e);
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    private Transaction generateRandomTxn(final TransactionType transactionType){
        return new Transaction(
                UUID.randomUUID().toString(),
                new Random().nextDouble() * (upperLimit - lowerLimit) + lowerLimit,
                transactionType
        );
    }

}
