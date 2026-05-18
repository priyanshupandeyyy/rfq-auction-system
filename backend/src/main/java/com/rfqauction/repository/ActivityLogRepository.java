package com.rfqauction.repository;

import com.rfqauction.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByAuction_IdOrderByCreatedAtDesc(Long auctionId);
}
