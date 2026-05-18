package com.rfq.dto;

import com.rfq.model.Auction;
import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class AuctionResponse {
    private Long id;
    private String title;
    private String description;
    private Long buyerId;
    private String buyerName;
    private String buyerCompany;
    private BigDecimal basePrice;
    private String currency;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime hardCloseTime;
    private Auction.Status status;
    private Auction.ExtensionTrigger extensionTrigger;
    private Integer extensionMinutes;
    private Integer extensionWindowMinutes;
    private LocalDateTime createdAt;
    private Long totalBids;
    private BigDecimal bestBidAmount;

    public static AuctionResponse from(Auction a) {
        return AuctionResponse.builder()
                .id(a.getId())
                .title(a.getTitle())
                .description(a.getDescription())
                .buyerId(a.getBuyer().getId())
                .buyerName(a.getBuyer().getName())
                .buyerCompany(a.getBuyer().getCompany())
                .basePrice(a.getBasePrice())
                .currency(a.getCurrency())
                .startTime(a.getStartTime())
                .endTime(a.getEndTime())
                .hardCloseTime(a.getHardCloseTime())
                .status(a.getStatus())
                .extensionTrigger(a.getExtensionTrigger())
                .extensionMinutes(a.getExtensionMinutes())
                .extensionWindowMinutes(a.getExtensionWindowMinutes())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
