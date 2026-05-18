package com.rfq.controller;

import com.rfq.dto.ApiResponse;
import com.rfq.dto.AuctionResponse;
import com.rfq.dto.CreateRfqRequest;
import com.rfq.service.RfqService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rfq")
@RequiredArgsConstructor
public class RfqController {

    private final RfqService rfqService;

    /** POST /api/rfq/create */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<AuctionResponse>> createRfq(@Valid @RequestBody CreateRfqRequest req) {
        AuctionResponse response = rfqService.createRfq(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("RFQ created successfully", response));
    }

    /** GET /api/rfq/all */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<AuctionResponse>>> getAllRfqs() {
        return ResponseEntity.ok(ApiResponse.success(rfqService.getAllRfqs()));
    }

    /** GET /api/rfq/active */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<AuctionResponse>>> getActiveRfqs() {
        return ResponseEntity.ok(ApiResponse.success(rfqService.getActiveRfqs()));
    }

    /** GET /api/rfq/:id */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuctionResponse>> getRfqById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(rfqService.getRfqById(id)));
    }

    /** PUT /api/rfq/close/:id */
    @PutMapping("/close/{id}")
    public ResponseEntity<ApiResponse<AuctionResponse>> closeRfq(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Auction closed successfully", rfqService.closeRfq(id)));
    }
}
