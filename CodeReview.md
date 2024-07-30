### Technical Exercise - Code Review

#### Aggregation Service

One of your colleagues has submitted the below class for a code review. Your objective is to perform the review which should cover all aspects of the implementation including (but not limited to) functional correctness and performance. Please document your comments and submit back to the recruiter.
Code Review Submission

```java

/**
    * Service to aggregate transactions tracking the overall balance for each account.
    * <p>
    * This service is expected to receive a significant number of transactions potentially from multiple threads.
 * */
  public class TransactionAggregationService {

    private final Map<String, Float> map = new HashMap<>(); // Map of Account Number to Balance

    /**
     * Process a transaction
     *
     * @param accountNumber     the account number the transaction relates to
     * @param transactionAmount the amount of the transaction (positive for credits, negative for debits)
     */
    public void processTransaction(String accountNumber, Float transactionAmount) throws Throwable {

        if (!accountNumber.startsWith("ACC")) {
            throw new Throwable("Account number invalid");
        } else if (accountNumber.length() != 8) {
            throw new Throwable("Account number invalid");
        } else if (transactionAmount > Float.MAX_VALUE) {
            throw new Throwable("Transaction amount invalid");
        }

        if (map.get(accountNumber) != null) {
            Float oldValue = map.get(accountNumber);
            map.put(accountNumber, oldValue + transactionAmount);
        } else {
            map.put(accountNumber, transactionAmount);
        }
    }

    public Float getBalance(String account) {
        return map.getOrDefault(account, -1F);
    }
}
  ```

### **REVIEW**

#### Comments/Suggestions
1. A better exception like `IllegalArgumentException` can be thrown instead of Throwable
2. The validation of account number can be separated into its own private method (which can be extended to add more validations or better all validation should be separated into a different validation class). And the method of validation can use a more robust alternative like regular expressions.
3. The validation check for `FLOAT.MAX_VALUE` won't be needed as incoming parameter value is already Float. Instead a better transaction amount threshold would be beneficial if that is a use-case.
4. The public methods should include initial null checks for incoming parameters.
5. To help with immutability we can make input as final. 
6. Returning `-1F` for non-existing accounts might not be intuitive. Returning throwing an exception might be clearer.
7. For thread safety, normal `HashMap` might cause issue. Instead using `ConcurrentHashMap` or synchronizing access to the map would be helpful. 
8. The map is accessed twice when updating the balance (`get` and `put`). This can be optimized to a single `compute` operation using `map.merge`  to update the balance, which reduces the number of map accesses and is thread-safe.
9. Proper tests also need to be put in place to test each line and their functionality properly. 
10. Appropriate Exception handling is also needed, either in the service or calling function or in a global handler.

### **Suggested Implementation**

```java
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import static org.apache.commons.lang3.Validate.notNull;

/**
 * Service to aggregate transactions tracking the overall balance for each account.
 * <p>
 * This service is expected to receive a significant number of transactions potentially from multiple threads.
 */
public class TransactionAggregationService {

    private final ConcurrentMap<String, Float> map = new ConcurrentHashMap<>(); // Map of Account Number to Balance

    /**
     * Process a transaction
     *
     * @param accountNumber     the account number the transaction relates to
     * @param transactionAmount the amount of the transaction (positive for credits, negative for debits)
     */
    public void processTransaction(final String accountNumber, final Float transactionAmount) {
       notNull(accountNumber, "accountNumber must not be null");
       notNull(transactionAmount, "transactionAmount must not be null");
       
       validateAccountNumber(accountNumber);
       map.merge(accountNumber, transactionAmount, Float::sum);
    }

    /**
     * Validate the account number.
     *
     * @param accountNumber the account number to validate
     */
    private void validateAccountNumber(final String accountNumber) {
        if (!accountNumber.matches("ACC\\d{5}")) { //this regex can be maintained in a separate constant file
            throw new IllegalArgumentException("Account number invalid");
        }
    }
    
    /**
     * Get the balance for an account.
     *
     * @param account the account number
     * @return the balance of the account
     */
    public Float getBalance(final String account) {
       notNull(accountNumber, "accountNumber must not be null");
       if(!map.containsKey(account)) throw new IllegalArgumentException("Account number is not present");
       return map.get(account);
    }
}
```


