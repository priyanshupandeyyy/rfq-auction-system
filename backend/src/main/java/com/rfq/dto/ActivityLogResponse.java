package com.rfq.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class ActivityLogResponse {
    private Long id;
    private Long rfqId;
    private String eventType;
    private String description;
    private Long actorId;
    private String metadata;
    private LocalDateTime createdAt;
}
