package com.rfq.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class SubmitBidRequest {

    @NotNull(message = "RFQ ID is required")
    private Long rfqId;

    @NotNull(message = "Supplier ID is required")
    private Long supplierId;

    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be positive")
    private BigDecimal amount;
}
