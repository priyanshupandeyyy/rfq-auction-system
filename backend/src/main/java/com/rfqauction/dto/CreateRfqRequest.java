package com.rfqauction.dto;

import com.rfqauction.model.Auction;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CreateRfqRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Created by (user ID) is required")
    private Long createdById;

    @NotNull(message = "Scheduled close time is required")
    @Future(message = "Scheduled close time must be in the future")
    private LocalDateTime scheduledCloseTime;

    @NotNull(message = "Hard close time is required")
    @Future(message = "Hard close time must be in the future")
    private LocalDateTime hardCloseTime;

    private Auction.ExtensionType extensionType = Auction.ExtensionType.TIME_BASED;
    private Integer extensionTriggerMins = 5;
    private Integer extensionDurationMins = 5;

    private String itemName;
    private BigDecimal quantity;
    private String unit;
    private BigDecimal basePrice;
    private String currency = "INR";
}
