package com.rfq.dto;

import com.rfq.model.Auction;
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

    @NotNull(message = "Buyer ID is required")
    private Long buyerId;

    private BigDecimal basePrice;

    private String currency = "INR";

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    @Future(message = "End time must be in the future")
    private LocalDateTime endTime;

    @NotNull(message = "Hard close time is required")
    @Future(message = "Hard close time must be in the future")
    private LocalDateTime hardCloseTime;

    private Auction.ExtensionTrigger extensionTrigger = Auction.ExtensionTrigger.TIME;

    private Integer extensionMinutes = 5;

    private Integer extensionWindowMinutes = 3;
}
