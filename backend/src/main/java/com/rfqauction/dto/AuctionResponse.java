package com.rfqauction.dto;

import com.rfqauction.model.Auction;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AuctionResponse {
    private Long id;
    private String rfqNumber;
    private String title;
    private String description;
    private String createdByUsername;
    private String createdByCompany;
    private Auction.AuctionStatus status;
    private LocalDateTime startTime;
    private LocalDateTime scheduledCloseTime;
    private LocalDateTime hardCloseTime;
    private Auction.ExtensionType extensionType;
    private Integer extensionTriggerMins;
    private Integer extensionDurationMins;
    private String itemName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal basePrice;
    private String currency;
    private LocalDateTime createdAt;
    private long totalBids;
    private long totalSuppliers;
    private BigDecimal currentL1Price;

    // Computed
    private boolean isExpired;
    private long secondsRemaining;
}
