package net.jpmchase.transaction.controller;

import net.jpmchase.transaction.producer.TransactionProducer;
import net.jpmchase.transaction.service.BankAccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@RestController
@RequestMapping("/account")
public class BankAccountController {

    private static final Logger logger = LogManager.getLogger(TransactionProducer.class);

    private final BankAccountService bankAccountService;

    public BankAccountController(final BankAccountService bankAccountService) {
        this.bankAccountService = bankAccountService;
    }

    @GetMapping("/balance")
    public ResponseEntity<Double> getBalance() {
        try {
            return ResponseEntity.ok(bankAccountService.retrieveBalance());
        }
        catch (final Exception ex) {
            logger.info("Error occurred while retrieving account balance");
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error occurred while retrieving account balance: " + ex.getMessage());
        }
    }
}
