package com.alcaniz.paymybuddy.audit;

import com.alcaniz.paymybuddy.model.FinancialOperationAudit;
import com.alcaniz.paymybuddy.repository.FinancialOperationAuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class FinancialOperationAuditListener {

    private final FinancialOperationAuditRepository financialOperationAuditRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFinancialOperationRecorded(FinancialOperationRecordedEvent event) {
        financialOperationAuditRepository.save(FinancialOperationAudit.fromEvent(
                event.operationType(),
                event.sourceType(),
                event.sourceId(),
                event.actorUserId(),
                event.accountId(),
                event.counterpartyAccountId(),
                event.amount(),
                event.fee(),
                event.currency(),
                event.description(),
                event.occurredAt()
        ));
    }
}
