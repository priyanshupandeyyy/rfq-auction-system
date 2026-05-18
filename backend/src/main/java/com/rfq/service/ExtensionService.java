package com.rfq.service;

import com.rfq.model.Auction;
import com.rfq.model.ActivityLog;
import com.rfq.repository.AuctionRepository;
import com.rfq.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Bid Time Extension Service
 *
 * Handles all three extension trigger modes:
 *   - TIME:     Bid submitted within `extensionWindowMinutes` of end_time → extend
 *   - RANK:     L1 changes (new cheapest bid) within window → extend
 *   - COMBINED: Both TIME and RANK conditions must be true → extend
 *
 * Hard constraint: new_end_time = MIN(end_time + extensionMinutes, hard_close_time)
 * The hard_close_time is NEVER violated.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExtensionService {

    private final AuctionRepository auctionRepository;
    private final ActivityLogRepository activityLogRepository;

    /**
     * Evaluate and apply bid time extension after a new bid is submitted.
     *
     * @param auction         The auction to evaluate
     * @param l1Changed       Whether the L1 (best) bid supplier changed due to this bid
     * @param bidSupplierId   The supplier who submitted the bid
     * @return true if the auction was extended
     */
    @Transactional
    public boolean evaluateAndExtend(Auction auction, boolean l1Changed, Long bidSupplierId) {
        if (auction.getStatus() != Auction.Status.ACTIVE) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endTime = auction.getEndTime();
        LocalDateTime hardClose = auction.getHardCloseTime();

        // Check if bid is within the extension window
        boolean withinWindow = now.isAfter(endTime.minusMinutes(auction.getExtensionWindowMinutes()));

        boolean shouldExtend = switch (auction.getExtensionTrigger()) {
            case TIME -> withinWindow;
            case RANK -> l1Changed && withinWindow;
            case COMBINED -> withinWindow && l1Changed;
        };

        if (!shouldExtend) {
            log.debug("No extension needed for auction {} (trigger={}, withinWindow={}, l1Changed={})",
                    auction.getId(), auction.getExtensionTrigger(), withinWindow, l1Changed);
            return false;
        }

        // Calculate new end time — NEVER exceed hard_close_time
        LocalDateTime proposedEndTime = endTime.plusMinutes(auction.getExtensionMinutes());
        LocalDateTime newEndTime = proposedEndTime.isBefore(hardClose) ? proposedEndTime : hardClose;

        if (!newEndTime.isAfter(endTime)) {
            // Already at or past hard close — cannot extend further
            log.info("Auction {} is at hard close limit, cannot extend further", auction.getId());
            logExtensionBlocked(auction, bidSupplierId);
            return false;
        }

        long minutesAdded = java.time.Duration.between(endTime, newEndTime).toMinutes();
        auction.setEndTime(newEndTime);
        auctionRepository.save(auction);

        String triggerDesc = switch (auction.getExtensionTrigger()) {
            case TIME -> "TIME trigger: bid placed within " + auction.getExtensionWindowMinutes() + " min of close";
            case RANK -> "RANK trigger: L1 changed within " + auction.getExtensionWindowMinutes() + " min of close";
            case COMBINED -> "COMBINED trigger: L1 changed AND bid within " + auction.getExtensionWindowMinutes() + " min";
        };

        String metadata = String.format(
                "{\"extensionMinutes\":%d,\"trigger\":\"%s\",\"newEndTime\":\"%s\",\"hardCloseTime\":\"%s\"}",
                minutesAdded,
                auction.getExtensionTrigger().name(),
                newEndTime,
                hardClose
        );

        logActivity(auction, ActivityLog.EVENT_AUCTION_EXTENDED, bidSupplierId,
                String.format("Auction extended by %d min. %s. New close: %s", minutesAdded, triggerDesc, newEndTime),
                metadata);

        log.info("Auction {} extended by {} min → new end: {} (hard close: {})",
                auction.getId(), minutesAdded, newEndTime, hardClose);

        return true;
    }

    private void logExtensionBlocked(Auction auction, Long actorId) {
        String desc = "Extension requested but auction is already at hard close limit: " + auction.getHardCloseTime();
        logActivity(auction, "EXTENSION_BLOCKED", actorId, desc, null);
    }

    private void logActivity(Auction auction, String eventType, Long actorId, String description, String metadata) {
        ActivityLog logEntry = ActivityLog.builder()
                .auction(auction)
                .eventType(eventType)
                .actorId(actorId)
                .description(description)
                .metadata(metadata)
                .build();
        activityLogRepository.save(logEntry);
    }
}
