package com.rfqauction.repository;

import com.rfqauction.model.Auction;
import com.rfqauction.model.Auction.AuctionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    List<Auction> findAllByOrderByCreatedAtDesc();

    List<Auction> findByStatusOrderByCreatedAtDesc(AuctionStatus status);

    Optional<Auction> findByRfqNumber(String rfqNumber);

    /** Auctions that are ACTIVE but whose scheduled_close_time has passed — needs auto-close */
    @Query("SELECT a FROM Auction a WHERE a.status = 'ACTIVE' AND a.scheduledCloseTime <= :now")
    List<Auction> findExpiredActiveAuctions(LocalDateTime now);
}
