package com.rfq.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RankingResponse {
    private Long rfqId;
    private String rfqTitle;
    private LocalDateTime endTime;
    private String status;
    private List<RankedBid> rankings;

    @Data
    @Builder
    public static class RankedBid {
        private Integer rank;
        private String label;   // L1, L2, L3 ...
        private Long supplierId;
        private String supplierName;
        private String supplierCompany;
        private BigDecimal amount;
        private String currency;
        private LocalDateTime submittedAt;
    }
}
