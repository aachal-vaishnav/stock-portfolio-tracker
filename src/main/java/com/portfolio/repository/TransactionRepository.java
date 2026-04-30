package com.portfolio.repository;

import com.portfolio.model.Transaction;
import com.portfolio.model.TransactionType;
import com.portfolio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserOrderByTransactionDateDesc(User user);

    List<Transaction> findByUserAndStockIdOrderByTransactionDateAsc(User user, Long stockId);

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.type = :type " +
           "AND t.transactionDate BETWEEN :startDate AND :endDate ORDER BY t.transactionDate ASC")
    List<Transaction> findByUserAndTypeAndDateBetween(@Param("user") User user,
                                                      @Param("type") TransactionType type,
                                                      @Param("startDate") LocalDate startDate,
                                                      @Param("endDate") LocalDate endDate);
}
