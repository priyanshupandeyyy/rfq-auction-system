package com.rfq.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BidResponse {
    private Long id;
    private Long rfqId;
    private Long supplierId;
    private String supplierName;
    private String supplierCompany;
    private BigDecimal amount;
    private Integer rankPosition;
    private LocalDateTime submittedAt;
    private Boolean isLatest;
}
