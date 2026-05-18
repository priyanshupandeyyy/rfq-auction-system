package com.rfq.repository;

import com.rfq.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    /** Get all latest bids for an auction, sorted by amount ascending (lowest = L1) */
    @Query("SELECT b FROM Bid b WHERE b.auction.id = :rfqId AND b.isLatest = true ORDER BY b.amount ASC")
    List<Bid> findLatestBidsByAuctionOrderByAmount(@Param("rfqId") Long rfqId);

    /** Get the current best bid (L1) for an auction */
    @Query("SELECT b FROM Bid b WHERE b.auction.id = :rfqId AND b.isLatest = true ORDER BY b.amount ASC LIMIT 1")
    Optional<Bid> findBestBidForAuction(@Param("rfqId") Long rfqId);

    /** Find latest bid by a specific supplier in an auction */
    @Query("SELECT b FROM Bid b WHERE b.auction.id = :rfqId AND b.supplier.id = :supplierId AND b.isLatest = true")
    Optional<Bid> findLatestBidBySupplierInAuction(@Param("rfqId") Long rfqId, @Param("supplierId") Long supplierId);

    /** Mark all previous bids by a supplier for an auction as not latest */
    @Modifying
    @Query("UPDATE Bid b SET b.isLatest = false WHERE b.auction.id = :rfqId AND b.supplier.id = :supplierId")
    void markPreviousBidsAsOld(@Param("rfqId") Long rfqId, @Param("supplierId") Long supplierId);

    /** All bids (including history) for an auction */
    List<Bid> findByAuctionIdOrderBySubmittedAtDesc(Long auctionId);
}
