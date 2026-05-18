package com.rfqauction.service;

import com.rfqauction.dto.BidResponse;
import com.rfqauction.dto.RankingEntry;
import com.rfqauction.dto.SubmitBidRequest;
import com.rfqauction.model.Auction;
import com.rfqauction.model.Bid;
import com.rfqauction.model.User;
import com.rfqauction.repository.BidRepository;
import com.rfqauction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BidService {

    private final BidRepository     bidRepository;
    private final UserRepository    userRepository;
    private final AuctionService    auctionService;

    @Transactional
    public BidResponse submitBid(SubmitBidRequest request) {
        Auction auction = auctionService.findAuction(request.getAuctionId());
        User    supplier = userRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + request.getSupplierId()));

        // Validate auction is active
        if (auction.getStatus() != Auction.AuctionStatus.ACTIVE) {
            throw new IllegalStateException("Auction " + auction.getRfqNumber() + " is not active");
        }

        // Validate before hard close time
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(auction.getHardCloseTime())) {
            throw new IllegalStateException("Hard close deadline has passed. No more bids accepted.");
        }

        // Validate before scheduled close time (current effective deadline)
        if (now.isAfter(auction.getScheduledCloseTime())) {
            throw new IllegalStateException("Auction deadline has passed. No more bids accepted.");
        }

        // Validate supplier role
        if (supplier.getRole() != User.Role.SUPPLIER) {
            throw new IllegalArgumentException("Only suppliers can submit bids");
        }

        // Evaluate extension BEFORE saving bid (uses current L1)
        auctionService.evaluateAndExtend(auction, request.getBidAmount(), supplier.getId());

        // Mark previous bids as not-latest
        bidRepository.markPreviousBidsAsNotLatest(auction.getId(), supplier.getId());

        // Get rank after extension evaluation (may have changed deadline but rankings are by amount)
        List<Bid> currentRanked = bidRepository.findByAuction_IdAndIsLatestTrueOrderByBidAmountAsc(auction.getId());
        int estimatedRank       = estimateNewRank(currentRanked, request.getBidAmount());

        Bid bid = Bid.builder()
                .auction(auction)
                .supplier(supplier)
                .bidAmount(request.getBidAmount())
                .remarks(request.getRemarks())
                .bidTime(now)
                .isLatest(true)
                .rankAtSubmit(estimatedRank)
                .build();

        bid = bidRepository.save(bid);

        // Activity log
        Map<String, Object> meta = new HashMap<>();
        meta.put("bidAmount", request.getBidAmount());
        meta.put("rank", estimatedRank);
        auctionService.logActivity(auction, "BID_SUBMITTED",
                supplier.getUsername() + " submitted bid of " + request.getBidAmount() + " " + auction.getCurrency() + " (Rank: " + estimatedRank + ")",
                supplier, meta);

        log.info("Bid submitted: auction={}, supplier={}, amount={}, rank={}",
                auction.getRfqNumber(), supplier.getUsername(), request.getBidAmount(), estimatedRank);

        return toBidResponse(bid);
    }

    @Transactional(readOnly = true)
    public List<BidResponse> getBidsForAuction(Long auctionId) {
        auctionService.findAuction(auctionId); // validate
        return bidRepository.findByAuction_IdOrderByBidTimeDesc(auctionId)
                .stream().map(this::toBidResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RankingEntry> getRankings(Long auctionId) {
        auctionService.findAuction(auctionId); // validate
        List<Bid> latestBids = bidRepository.findByAuction_IdAndIsLatestTrueOrderByBidAmountAsc(auctionId);

        List<RankingEntry> rankings = new ArrayList<>();
        for (int i = 0; i < latestBids.size(); i++) {
            Bid b = latestBids.get(i);
            rankings.add(RankingEntry.builder()
                    .rank(i + 1)
                    .supplierId(b.getSupplier().getId())
                    .supplierUsername(b.getSupplier().getUsername())
                    .supplierCompany(b.getSupplier().getCompany())
                    .bidAmount(b.getBidAmount())
                    .bidTime(b.getBidTime())
                    .remarks(b.getRemarks())
                    .build());
        }
        return rankings;
    }

    private int estimateNewRank(List<Bid> currentRanked, java.math.BigDecimal newAmount) {
        int rank = 1;
        for (Bid b : currentRanked) {
            if (newAmount.compareTo(b.getBidAmount()) > 0) {
                rank++;
            }
        }
        return rank;
    }

    private BidResponse toBidResponse(Bid b) {
        return BidResponse.builder()
                .id(b.getId())
                .auctionId(b.getAuction().getId())
                .rfqNumber(b.getAuction().getRfqNumber())
                .supplierId(b.getSupplier().getId())
                .supplierUsername(b.getSupplier().getUsername())
                .supplierCompany(b.getSupplier().getCompany())
                .bidAmount(b.getBidAmount())
                .remarks(b.getRemarks())
                .bidTime(b.getBidTime())
                .isLatest(b.getIsLatest())
                .rankAtSubmit(b.getRankAtSubmit())
                .build();
    }
}
