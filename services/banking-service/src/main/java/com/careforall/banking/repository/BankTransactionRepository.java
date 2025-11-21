package com.careforall.banking.repository;

import com.careforall.banking.entity.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Bank Transaction Repository
 */
@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    List<BankTransaction> findByAccountIdOrderByCreatedAtDesc(Long accountId);

    Optional<BankTransaction> findByExternalReference(String externalReference);

    List<BankTransaction> findByExternalReferenceOrderByCreatedAtDesc(String externalReference);
}
