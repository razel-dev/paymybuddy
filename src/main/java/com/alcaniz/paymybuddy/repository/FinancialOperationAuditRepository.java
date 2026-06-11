package com.alcaniz.paymybuddy.repository;

import com.alcaniz.paymybuddy.model.FinancialOperationAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialOperationAuditRepository extends JpaRepository<FinancialOperationAudit, Integer> {
}
