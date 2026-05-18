package com.rfqauction.service;

import com.rfqauction.dto.*;
import com.rfqauction.model.*;
import com.rfqauction.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuctionService {

    private final AuctionRepository    auctionRepository;
    private final UserRepository       userRepository;
    private final BidRepository        bidRepository;
    private final ActivityLogRepository activityLogRepository;

    // ─── RFQ / Auction CRUD ──────────────────────────────────────────────────

    @Transactional
    public AuctionResponse createRfq(CreateRfqRequest request) {
        User creator = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + request.getCreatedById()));

        if (!request.getHardCloseTime().isAfter(request.getScheduledCloseTime())) {
            throw new IllegalArgumentException("Hard close time must be after scheduled close time");
        }

        String rfqNumber = generateRfqNumber();

        Auction auction = Auction.builder()
                .rfqNumber(rfqNumber)
                .title(request.getTitle())
                .description(request.getDescription())
                .createdBy(creator)
                .status(Auction.AuctionStatus.ACTIVE)
                .startTime(LocalDateTime.now())
                .scheduledCloseTime(request.getScheduledCloseTime())
                .hardCloseTime(request.getHardCloseTime())
                .extensionType(request.getExtensionType() != null ? request.getExtensionType() : Auction.ExtensionType.TIME_BASED)
                .extensionTriggerMins(request.getExtensionTriggerMins() != null ? request.getExtensionTriggerMins() : 5)
                .extensionDurationMins(request.getExtensionDurationMins() != null ? request.getExtensionDurationMins() : 5)
                .itemName(request.getItemName())
                .quantity(request.getQuantity())
                .unit(request.getUnit())
                .basePrice(request.getBasePrice())
                .currency(request.getCurrency() != null ? request.getCurrency() : "INR")
                .build();

        auction = auctionRepository.save(auction);
        logActivity(auction, "AUCTION_CREATED", "Auction " + rfqNumber + " created by " + creator.getUsername(), creator, null);

        return toAuctionResponse(auction);
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> getAllAuctions() {
        return auctionRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toAuctionResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuctionResponse> getActiveAuctions() {
        return auctionRepository.findByStatusOrderByCreatedAtDesc(Auction.AuctionStatus.ACTIVE)
                .stream().map(this::toAuctionResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AuctionResponse getAuctionById(Long id) {
        Auction auction = findAuction(id);
        return toAuctionResponse(auction);
    }

    @Transactional
    public AuctionResponse closeAuction(Long id) {
        Auction auction = findAuction(id);
        if (auction.getStatus() != Auction.AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Auction is not active");
        }
        auction.setStatus(Auction.AuctionStatus.CLOSED);
        auction = auctionRepository.save(auction);
        logActivity(auction, "AUCTION_CLOSED", "Auction " + auction.getRfqNumber() + " force-closed by system/admin", null, null);
        return toAuctionResponse(auction);
    }

    // ─── Bid Extension Logic ─────────────────────────────────────────────────

    /**
     * Core extension engine — evaluates whether an incoming bid should extend
     * the auction's scheduled close time.
     *
     * Rules:
     *  TIME_BASED  — bid placed within X mins of scheduled close → extend
     *  RANK_BASED  — the new bid changes the L1 supplier → extend
     *  COMBINED    — both conditions must be true → extend
     *
     * The hard_close_time is NEVER exceeded.
     */
    @Transactional
    public boolean evaluateAndExtend(Auction auction, BigDecimal newBidAmount, Long supplierId) {
        LocalDateTime now = LocalDateTime.now();

        boolean timeTrigger  = false;
        boolean rankTrigger  = false;

        // ── Time-based trigger ────────────────────────────────────────
        long minsToClose = ChronoUnit.MINUTES.between(now, auction.getScheduledCloseTime());
        if (minsToClose >= 0 && minsToClose <= auction.getExtensionTriggerMins()) {
            timeTrigger = true;
        }

        // ── Rank-based trigger ────────────────────────────────────────
        Optional<Bid> currentL1 = bidRepository.findTopByAuction_IdAndIsLatestTrueOrderByBidAmountAsc(auction.getId());
        if (currentL1.isEmpty() || newBidAmount.compareTo(currentL1.get().getBidAmount()) < 0) {
            // This bid will become the new L1 (or first bid)
            rankTrigger = true;
        }

        boolean shouldExtend = switch (auction.getExtensionType()) {
            case TIME_BASED -> timeTrigger;
            case RANK_BASED -> rankTrigger;
            case COMBINED   -> timeTrigger && rankTrigger;
        };

        if (shouldExtend) {
            LocalDateTime proposedClose = auction.getScheduledCloseTime()
                    .plusMinutes(auction.getExtensionDurationMins());

            // Hard deadline must never be violated
            LocalDateTime newClose = proposedClose.isAfter(auction.getHardCloseTime())
                    ? auction.getHardCloseTime()
                    : proposedClose;

            if (newClose.isAfter(auction.getScheduledCloseTime())) {
                LocalDateTime oldClose = auction.getScheduledCloseTime();
                auction.setScheduledCloseTime(newClose);
                auctionRepository.save(auction);

                Map<String, Object> meta = new HashMap<>();
                meta.put("oldScheduledClose", oldClose.toString());
                meta.put("newScheduledClose", newClose.toString());
                meta.put("trigger", auction.getExtensionType().name());
                logActivity(auction, "AUCTION_EXTENDED",
                        "Deadline extended from " + oldClose + " to " + newClose + " (" + auction.getExtensionType() + " trigger)",
                        null, meta);
                log.info("Auction {} extended to {}", auction.getRfqNumber(), newClose);
                return true;
            }
        }
        return false;
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    public Auction findAuction(Long id) {
        return auctionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Auction not found: " + id));
    }

    public void logActivity(Auction auction, String eventType, String description, User actor, Map<String, Object> metadata) {
        ActivityLog log = ActivityLog.builder()
                .auction(auction)
                .eventType(eventType)
                .description(description)
                .actor(actor)
                .metadata(metadata)
                .build();
        activityLogRepository.save(log);
    }

    public List<ActivityLogResponse> getActivityLogs(Long auctionId) {
        findAuction(auctionId); // validate
        return activityLogRepository.findByAuction_IdOrderByCreatedAtDesc(auctionId)
                .stream().map(this::toActivityResponse).collect(Collectors.toList());
    }

    private AuctionResponse toAuctionResponse(Auction a) {
        List<Bid> latestBids = bidRepository.findByAuction_IdAndIsLatestTrueOrderByBidAmountAsc(a.getId());
        long totalBids       = bidRepository.findByAuction_IdOrderByBidTimeDesc(a.getId()).size();
        BigDecimal l1Price   = latestBids.isEmpty() ? null : latestBids.get(0).getBidAmount();

        LocalDateTime now      = LocalDateTime.now();
        boolean isExpired      = a.getScheduledCloseTime().isBefore(now);
        long secondsRemaining  = Math.max(0, ChronoUnit.SECONDS.between(now, a.getScheduledCloseTime()));

        return AuctionResponse.builder()
                .id(a.getId())
                .rfqNumber(a.getRfqNumber())
                .title(a.getTitle())
                .description(a.getDescription())
                .createdByUsername(a.getCreatedBy().getUsername())
                .createdByCompany(a.getCreatedBy().getCompany())
                .status(a.getStatus())
                .startTime(a.getStartTime())
                .scheduledCloseTime(a.getScheduledCloseTime())
                .hardCloseTime(a.getHardCloseTime())
                .extensionType(a.getExtensionType())
                .extensionTriggerMins(a.getExtensionTriggerMins())
                .extensionDurationMins(a.getExtensionDurationMins())
                .itemName(a.getItemName())
                .quantity(a.getQuantity())
                .unit(a.getUnit())
                .basePrice(a.getBasePrice())
                .currency(a.getCurrency())
                .createdAt(a.getCreatedAt())
                .totalBids(totalBids)
                .totalSuppliers(latestBids.size())
                .currentL1Price(l1Price)
                .isExpired(isExpired)
                .secondsRemaining(secondsRemaining)
                .build();
    }

    private ActivityLogResponse toActivityResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .auctionId(log.getAuction().getId())
                .eventType(log.getEventType())
                .description(log.getDescription())
                .actorUsername(log.getActor() != null ? log.getActor().getUsername() : "System")
                .metadata(log.getMetadata())
                .createdAt(log.getCreatedAt())
                .build();
    }

    private String generateRfqNumber() {
        String year = String.valueOf(LocalDateTime.now().getYear());
        long count  = auctionRepository.count() + 1;
        return String.format("RFQ-%s-%03d", year, count);
    }
}
