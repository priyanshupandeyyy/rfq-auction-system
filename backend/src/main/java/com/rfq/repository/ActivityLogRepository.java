package com.rfq.repository;

import com.rfq.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findByAuctionIdOrderByCreatedAtDesc(Long auctionId);
}
