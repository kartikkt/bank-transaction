package net.jpmchase.transaction.model;

import net.jpmchase.transaction.enums.TransactionType;;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@ExtendWith(MockitoExtension.class)
class TransactionTest {

    private final String id = "id";
    private final double amount = 0.0;
    private final TransactionType transactionType = TransactionType.CREDIT;

    private final Supplier<Transaction> populatedSupplier = () -> new Transaction(id, amount, transactionType);

    @Nested
    class PreConditions {

        @Test
        public void idMustNotBeNull(){
            assertThatNullPointerException()
                    .isThrownBy(() -> new Transaction(null, amount, transactionType))
                    .withMessage("id must not be null");
        }

        @Test
        public void transactionTypeMustNotBeNull(){
            assertThatNullPointerException()
                    .isThrownBy(() -> new Transaction(id, amount, null))
                    .withMessage("transactionType must not be null");
        }
    }

    @Test
    public void FieldsPopulatedWithExpectedValue(){
        assertThat(populatedSupplier.get())
                .extracting(Transaction::getAmount,
                        Transaction::getId,
                        Transaction::getTransactionType)
                .containsExactly(amount, id, transactionType);
    }
}