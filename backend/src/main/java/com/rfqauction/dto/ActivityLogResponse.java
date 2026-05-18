package com.rfqauction.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ActivityLogResponse {
    private Long id;
    private Long auctionId;
    private String eventType;
    private String description;
    private String actorUsername;
    private Map<String, Object> metadata;
    private LocalDateTime createdAt;
}
