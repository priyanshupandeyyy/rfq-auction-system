package com.rfqauction.controller;

import com.rfqauction.dto.ActivityLogResponse;
import com.rfqauction.dto.AuctionResponse;
import com.rfqauction.dto.CreateRfqRequest;
import com.rfqauction.service.AuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RfqController {

    private final AuctionService auctionService;

    /** POST /api/rfq/create — Create a new RFQ auction */
    @PostMapping("/rfq/create")
    public ResponseEntity<?> createRfq(@Valid @RequestBody CreateRfqRequest request) {
        try {
            AuctionResponse response = auctionService.createRfq(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET /api/rfq/all — List all auctions */
    @GetMapping("/rfq/all")
    public ResponseEntity<List<AuctionResponse>> getAllAuctions() {
        return ResponseEntity.ok(auctionService.getAllAuctions());
    }

    /** GET /api/rfq/active — List active auctions only */
    @GetMapping("/rfq/active")
    public ResponseEntity<List<AuctionResponse>> getActiveAuctions() {
        return ResponseEntity.ok(auctionService.getActiveAuctions());
    }

    /** GET /api/rfq/{id} — Get auction by ID */
    @GetMapping("/rfq/{id}")
    public ResponseEntity<?> getAuction(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(auctionService.getAuctionById(id));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    /** PUT /api/rfq/close/{id} — Force-close an auction */
    @PutMapping("/rfq/close/{id}")
    public ResponseEntity<?> closeAuction(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(auctionService.closeAuction(id));
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET /api/activity/{rfqId} — Activity log for an auction */
    @GetMapping("/activity/{rfqId}")
    public ResponseEntity<?> getActivityLog(@PathVariable Long rfqId) {
        try {
            List<ActivityLogResponse> logs = auctionService.getActivityLogs(rfqId);
            return ResponseEntity.ok(logs);
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}
