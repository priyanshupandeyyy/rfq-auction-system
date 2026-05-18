package com.rfq.service;

import com.rfq.dto.AuctionResponse;
import com.rfq.dto.CreateRfqRequest;
import com.rfq.model.Auction;
import com.rfq.model.ActivityLog;
import com.rfq.model.User;
import com.rfq.repository.AuctionRepository;
import com.rfq.repository.ActivityLogRepository;
import com.rfq.repository.BidRepository;
import com.rfq.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RfqService {

    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final BidRepository bidRepository;

    @Transactional
    public AuctionResponse createRfq(CreateRfqRequest req) {
        User buyer = userRepository.findById(req.getBuyerId())
                .orElseThrow(() -> new IllegalArgumentException("Buyer not found: " + req.getBuyerId()));

        if (!buyer.getRole().equals(User.Role.BUYER)) {
            throw new IllegalArgumentException("User is not a BUYER");
        }

        if (!req.getHardCloseTime().isAfter(req.getEndTime())) {
            throw new IllegalArgumentException("Hard close time must be after end time");
        }

        Auction auction = Auction.builder()
                .title(req.getTitle())
                .description(req.getDescription())
                .buyer(buyer)
                .basePrice(req.getBasePrice())
                .currency(req.getCurrency() != null ? req.getCurrency() : "INR")
                .startTime(req.getStartTime())
                .endTime(req.getEndTime())
                .hardCloseTime(req.getHardCloseTime())
                .status(Auction.Status.ACTIVE)
                .extensionTrigger(req.getExtensionTrigger() != null ? req.getExtensionTrigger() : Auction.ExtensionTrigger.TIME)
                .extensionMinutes(req.getExtensionMinutes() != null ? req.getExtensionMinutes() : 5)
                .extensionWindowMinutes(req.getExtensionWindowMinutes() != null ? req.getExtensionWindowMinutes() : 3)
                .build();

        auction = auctionRepository.save(auction);

        // Log creation
        logActivity(auction, ActivityLog.EVENT_AUCTION_CREATED, null,
                "RFQ auction created: " + auction.getTitle(), null);

        return AuctionResponse.from(auction);
    }

    public List<AuctionResponse> getAllRfqs() {
        return auctionRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(a -> {
                    AuctionResponse r = AuctionResponse.from(a);
                    r.setTotalBids((long) bidRepository.findByAuctionIdOrderBySubmittedAtDesc(a.getId())
                            .stream().filter(b -> Boolean.TRUE.equals(b.getIsLatest())).count());
                    bidRepository.findBestBidForAuction(a.getId())
                            .ifPresent(b -> r.setBestBidAmount(b.getAmount()));
                    return r;
                })
                .collect(Collectors.toList());
    }

    public AuctionResponse getRfqById(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + id));
        AuctionResponse r = AuctionResponse.from(auction);
        r.setTotalBids((long) bidRepository.findLatestBidsByAuctionOrderByAmount(auction.getId()).size());
        bidRepository.findBestBidForAuction(auction.getId())
                .ifPresent(b -> r.setBestBidAmount(b.getAmount()));
        return r;
    }

    public List<AuctionResponse> getActiveRfqs() {
        return auctionRepository.findByStatus(Auction.Status.ACTIVE)
                .stream()
                .map(a -> {
                    AuctionResponse r = AuctionResponse.from(a);
                    r.setTotalBids((long) bidRepository.findLatestBidsByAuctionOrderByAmount(a.getId()).size());
                    bidRepository.findBestBidForAuction(a.getId())
                            .ifPresent(b -> r.setBestBidAmount(b.getAmount()));
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public AuctionResponse closeRfq(Long id) {
        Auction auction = auctionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + id));

        if (auction.getStatus() == Auction.Status.CLOSED) {
            throw new IllegalStateException("Auction is already closed");
        }

        auction.setStatus(Auction.Status.CLOSED);
        auction = auctionRepository.save(auction);

        logActivity(auction, ActivityLog.EVENT_AUCTION_CLOSED, null,
                "Auction manually closed by buyer", null);

        return AuctionResponse.from(auction);
    }

    /** Scheduled job: auto-close auctions that have passed their endTime */
    @Scheduled(fixedRate = 30000) // every 30 seconds
    @Transactional
    public void autoCloseExpiredAuctions() {
        List<Auction> expired = auctionRepository.findExpiredActiveAuctions(LocalDateTime.now());
        for (Auction auction : expired) {
            auction.setStatus(Auction.Status.CLOSED);
            auctionRepository.save(auction);
            logActivity(auction, ActivityLog.EVENT_AUCTION_CLOSED, null,
                    "Auction auto-closed after reaching end time", null);
            log.info("Auto-closed auction: {} (id={})", auction.getTitle(), auction.getId());
        }
    }

    private void logActivity(Auction auction, String eventType, Long actorId, String description, String metadata) {
        ActivityLog log = ActivityLog.builder()
                .auction(auction)
                .eventType(eventType)
                .actorId(actorId)
                .description(description)
                .metadata(metadata)
                .build();
        activityLogRepository.save(log);
    }
}
