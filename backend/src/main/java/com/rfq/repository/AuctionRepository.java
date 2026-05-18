package com.rfq.repository;

import com.rfq.model.Auction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {

    List<Auction> findByStatus(Auction.Status status);

    @Query("SELECT a FROM Auction a WHERE a.status = 'ACTIVE' AND a.endTime <= :now")
    List<Auction> findExpiredActiveAuctions(LocalDateTime now);

    List<Auction> findAllByOrderByCreatedAtDesc();
}
