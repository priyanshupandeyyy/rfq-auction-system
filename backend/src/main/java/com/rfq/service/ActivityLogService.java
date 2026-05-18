package com.rfq.service;

import com.rfq.dto.ActivityLogResponse;
import com.rfq.model.ActivityLog;
import com.rfq.repository.ActivityLogRepository;
import com.rfq.repository.AuctionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;
    private final AuctionRepository auctionRepository;

    public List<ActivityLogResponse> getActivityForAuction(Long rfqId) {
        auctionRepository.findById(rfqId)
                .orElseThrow(() -> new IllegalArgumentException("Auction not found: " + rfqId));

        return activityLogRepository.findByAuctionIdOrderByCreatedAtDesc(rfqId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ActivityLogResponse toResponse(ActivityLog log) {
        return ActivityLogResponse.builder()
                .id(log.getId())
                .rfqId(log.getAuction().getId())
                .eventType(log.getEventType())
                .description(log.getDescription())
                .actorId(log.getActorId())
                .metadata(log.getMetadata())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
