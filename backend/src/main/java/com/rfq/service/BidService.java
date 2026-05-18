package com.rfq.service;

import com.rfq.dto.BidResponse;
import com.rfq.dto.RankingResponse;
import com.rfq.dto.SubmitBidRequest;
import com.rfq.model.*;
import com.rfq.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;
    private final ExtensionService extensionService;

    @Transactional
    public BidResponse submitBid(SubmitBidRequest req) {
        Auction auction = auctionRepository.findById(req.getRfqId())
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + req.getRfqId()));

        if (auction.getStatus() != Auction.Status.ACTIVE) {
            throw new IllegalStateException("Auction is not active");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auction.getEndTime())) {
            throw new IllegalStateException("Auction has closed — bids are no longer accepted");
        }

        User supplier = userRepository.findById(req.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + req.getSupplierId()));

        if (!supplier.getRole().equals(User.Role.SUPPLIER)) {
            throw new IllegalArgumentException("User is not a SUPPLIER");
        }

        // Capture current L1 before this bid
        Optional<Bid> currentL1 = bidRepository.findBestBidForAuction(auction.getId());
        Long previousL1SupplierId = currentL1.map(b -> b.getSupplier().getId()).orElse(null);

        // Mark previous bids by this supplier as not latest
        bidRepository.markPreviousBidsAsOld(auction.getId(), supplier.getId());

        // Save the new bid
        Bid bid = Bid.builder()
                .auction(auction)
                .supplier(supplier)
                .amount(req.getAmount())
                .isLatest(true)
                .build();
        bid = bidRepository.save(bid);

        // Recalculate ranks for all latest bids
        recalculateRanks(auction.getId());

        // Reload bid with updated rank
        bid = bidRepository.findById(bid.getId()).orElseThrow();

        // Determine if L1 changed
        Optional<Bid> newL1 = bidRepository.findBestBidForAuction(auction.getId());
        boolean l1Changed = newL1.map(b -> !b.getSupplier().getId().equals(previousL1SupplierId)).orElse(false);

        // Log the bid submission
        String metadata = String.format(
                "{\"amount\":%s,\"rankPosition\":%d,\"l1Changed\":%b}",
                req.getAmount(), bid.getRankPosition(), l1Changed);

        logActivity(auction, ActivityLog.EVENT_BID_SUBMITTED, supplier.getId(),
                String.format("%s submitted bid of %s %s (Rank: L%d)",
                        supplier.getName(), auction.getCurrency(), req.getAmount(), bid.getRankPosition()),
                metadata);

        if (l1Changed) {
            logActivity(auction, ActivityLog.EVENT_RANK_CHANGED, supplier.getId(),
                    String.format("L1 changed to %s with bid %s", supplier.getName(), req.getAmount()), null);
        }

        // Evaluate time extension
        extensionService.evaluateAndExtend(auction, l1Changed, supplier.getId());

        return toBidResponse(bid);
    }

    public List<BidResponse> getBidsForAuction(Long rfqId) {
        auctionRepository.findById(rfqId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + rfqId));
        return bidRepository.findByAuctionIdOrderBySubmittedAtDesc(rfqId)
                .stream()
                .map(this::toBidResponse)
                .collect(Collectors.toList());
    }

    public RankingResponse getRankings(Long rfqId) {
        Auction auction = auctionRepository.findById(rfqId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + rfqId));

        List<Bid> rankedBids = bidRepository.findLatestBidsByAuctionOrderByAmount(rfqId);

        List<RankingResponse.RankedBid> rankedList = new ArrayList<>();
        for (int i = 0; i < rankedBids.size(); i++) {
            Bid b = rankedBids.get(i);
            rankedList.add(RankingResponse.RankedBid.builder()
                    .rank(i + 1)
                    .label("L" + (i + 1))
                    .supplierId(b.getSupplier().getId())
                    .supplierName(b.getSupplier().getName())
                    .supplierCompany(b.getSupplier().getCompany())
                    .amount(b.getAmount())
                    .currency(auction.getCurrency())
                    .submittedAt(b.getSubmittedAt())
                    .build());
        }

        return RankingResponse.builder()
                .rfqId(rfqId)
                .rfqTitle(auction.getTitle())
                .endTime(auction.getEndTime())
                .status(auction.getStatus().name())
                .rankings(rankedList)
                .build();
    }

    /** Recalculate and persist rank positions for all latest bids in an auction */
    @Transactional
    public void recalculateRanks(Long rfqId) {
        List<Bid> bids = bidRepository.findLatestBidsByAuctionOrderByAmount(rfqId);
        for (int i = 0; i < bids.size(); i++) {
            bids.get(i).setRankPosition(i + 1);
            bidRepository.save(bids.get(i));
        }
    }

    private BidResponse toBidResponse(Bid b) {
        return BidResponse.builder()
                .id(b.getId())
                .rfqId(b.getAuction().getId())
                .supplierId(b.getSupplier().getId())
                .supplierName(b.getSupplier().getName())
                .supplierCompany(b.getSupplier().getCompany())
                .amount(b.getAmount())
                .rankPosition(b.getRankPosition())
                .submittedAt(b.getSubmittedAt())
                .isLatest(b.getIsLatest())
                .build();
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
