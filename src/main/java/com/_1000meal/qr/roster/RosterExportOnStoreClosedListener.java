package com._1000meal.qr.roster;

import com._1000meal.store.event.StoreClosedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

//매장이 닫히는 시점에 통합 CSV 생성 및 Sheets 동기화를 수행.
@Slf4j
@Component
@RequiredArgsConstructor
public class RosterExportOnStoreClosedListener {

    private final RosterExportJob rosterExportJob;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onStoreClosed(StoreClosedEvent event) {
        log.info("[STORE CLOSED] storeId={}, date={} -> roster export+sync", event.storeId(), event.date());
        rosterExportJob.runOnce(event.date());
    }
}

