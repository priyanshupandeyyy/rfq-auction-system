package com.rfqauction.repository;

import com.rfqauction.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    /** All bids for an auction ordered by time */
    List<Bid> findByAuction_IdOrderByBidTimeDesc(Long auctionId);

    /** Only the latest bid per supplier (used for ranking) */
    List<Bid> findByAuction_IdAndIsLatestTrueOrderByBidAmountAsc(Long auctionId);

    /** Current best (lowest) bid for an auction */
    Optional<Bid> findTopByAuction_IdAndIsLatestTrueOrderByBidAmountAsc(Long auctionId);

    /** Latest bid of a specific supplier for an auction */
    Optional<Bid> findTopByAuction_IdAndSupplier_IdAndIsLatestTrueOrderByBidTimeDesc(Long auctionId, Long supplierId);

    /** Mark all previous bids of this supplier in this auction as not-latest */
    @Modifying
    @Transactional
    @Query("UPDATE Bid b SET b.isLatest = false WHERE b.auction.id = :auctionId AND b.supplier.id = :supplierId AND b.isLatest = true")
    void markPreviousBidsAsNotLatest(Long auctionId, Long supplierId);

    /** Count of distinct suppliers who have bid on an auction */
    @Query("SELECT COUNT(DISTINCT b.supplier.id) FROM Bid b WHERE b.auction.id = :auctionId AND b.isLatest = true")
    long countDistinctSuppliersForAuction(Long auctionId);
}
