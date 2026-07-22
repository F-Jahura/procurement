package com.fatema.procurement.repository;

import com.fatema.procurement.entity.GoodsReceipt;
import com.fatema.procurement.entity.ReceiptStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, Long> {

    List<GoodsReceipt> findByStatus(ReceiptStatus status);

    List<GoodsReceipt> findByReceiptDateBetween(LocalDate startDate, LocalDate endDate);

    @Query("SELECT COUNT(r) FROM GoodsReceipt r WHERE r.status = :status")
    long countByStatus(@Param("status") ReceiptStatus status);

    @Query("SELECT COUNT(r) FROM GoodsReceipt r WHERE r.receiptDate = :date")
    long countByDate(@Param("date") LocalDate date);

    @Query("SELECT SUM(r.totalAmount) FROM GoodsReceipt r WHERE r.receiptDate BETWEEN :startDate AND :endDate AND r.status = 'CONFIRMED'")
    BigDecimal sumTotalAmountBetweenDates(@Param("startDate") LocalDate startDate,
                                          @Param("endDate") LocalDate endDate);

    List<GoodsReceipt> findByPurchaseOrderId(Long purchaseOrderId);
}
