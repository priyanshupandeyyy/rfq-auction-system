package com.rfqauction.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class BidResponse {
    private Long id;
    private Long auctionId;
    private String rfqNumber;
    private Long supplierId;
    private String supplierUsername;
    private String supplierCompany;
    private BigDecimal bidAmount;
    private String remarks;
    private LocalDateTime bidTime;
    private Boolean isLatest;
    private Integer rankAtSubmit;
}
