package com.careforall.banking.repository;

import com.careforall.banking.entity.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

/**
 * Bank Account Repository
 *
 * Uses pessimistic locking to prevent race conditions during balance updates
 */
@Repository
public interface BankAccountRepository extends JpaRepository<BankAccount, Long> {

    Optional<BankAccount> findByEmail(String email);

    Optional<BankAccount> findByUserId(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BankAccount> findByEmailForUpdate(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BankAccount> findByUserIdForUpdate(Long userId);
}
