package com.rfqauction.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RankingEntry {
    private int rank;           // 1 = L1, 2 = L2, 3 = L3 ...
    private Long supplierId;
    private String supplierUsername;
    private String supplierCompany;
    private BigDecimal bidAmount;
    private LocalDateTime bidTime;
    private String remarks;
}
