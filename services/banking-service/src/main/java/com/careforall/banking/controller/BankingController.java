package com.careforall.banking.controller;

import com.careforall.banking.entity.BankAccount;
import com.careforall.banking.entity.BankTransaction;
import com.careforall.banking.repository.BankAccountRepository;
import com.careforall.banking.repository.BankTransactionRepository;
import com.careforall.banking.service.BankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Banking Controller
 *
 * REST API for admin operations and testing
 */
@RestController
@RequestMapping("/api/banking")
@RequiredArgsConstructor
@Slf4j
public class BankingController {

    private final BankingService bankingService;
    private final BankAccountRepository accountRepository;
    private final BankTransactionRepository transactionRepository;

    /**
     * Get account by email
     */
    @GetMapping("/accounts/{email}")
    public ResponseEntity<BankAccount> getAccount(@PathVariable String email) {
        return accountRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Get all accounts (admin only)
     */
    @GetMapping("/accounts")
    public ResponseEntity<List<BankAccount>> getAllAccounts() {
        return ResponseEntity.ok(accountRepository.findAll());
    }

    /**
     * Get account transactions
     */
    @GetMapping("/accounts/{email}/transactions")
    public ResponseEntity<List<BankTransaction>> getAccountTransactions(@PathVariable String email) {
        BankAccount account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        List<BankTransaction> transactions = transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId());
        return ResponseEntity.ok(transactions);
    }

    /**
     * Add funds to account (for testing/demo purposes)
     */
    @PostMapping("/accounts/{email}/add-funds")
    public ResponseEntity<Map<String, Object>> addFunds(
            @PathVariable String email,
            @RequestParam BigDecimal amount) {

        bankingService.addFunds(email, amount);

        BankAccount account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return ResponseEntity.ok(Map.of(
                "message", "Funds added successfully",
                "email", email,
                "amount", amount,
                "newBalance", account.getAvailableBalance()
        ));
    }

    /**
     * Create dummy account (for testing)
     */
    @PostMapping("/accounts")
    public ResponseEntity<BankAccount> createAccount(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam(required = false, defaultValue = "10000") BigDecimal initialBalance) {

        BankAccount account = BankAccount.builder()
                .email(email)
                .accountHolderName(name)
                .availableBalance(initialBalance)
                .lockedBalance(BigDecimal.ZERO)
                .build();

        BankAccount saved = accountRepository.save(account);
        log.info("Created account for {} with balance {}", email, initialBalance);

        return ResponseEntity.ok(saved);
    }

    /**
     * Health check
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Banking Service",
                "version", "2.0"
        ));
    }
}
