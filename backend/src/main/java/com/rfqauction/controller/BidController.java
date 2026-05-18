package com.rfqauction.controller;

import com.rfqauction.dto.BidResponse;
import com.rfqauction.dto.RankingEntry;
import com.rfqauction.dto.SubmitBidRequest;
import com.rfqauction.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    /** POST /api/bid/submit — Submit a new bid */
    @PostMapping("/bid/submit")
    public ResponseEntity<?> submitBid(@Valid @RequestBody SubmitBidRequest request) {
        try {
            BidResponse response = bidService.submitBid(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** GET /api/bid/{rfqId} — All bids for an auction */
    @GetMapping("/bid/{rfqId}")
    public ResponseEntity<?> getBids(@PathVariable Long rfqId) {
        try {
            List<BidResponse> bids = bidService.getBidsForAuction(rfqId);
            return ResponseEntity.ok(bids);
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }

    /** GET /api/ranking/{rfqId} — L1/L2/L3 ranked bids */
    @GetMapping("/ranking/{rfqId}")
    public ResponseEntity<?> getRankings(@PathVariable Long rfqId) {
        try {
            List<RankingEntry> rankings = bidService.getRankings(rfqId);
            return ResponseEntity.ok(rankings);
        } catch (java.util.NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("error", e.getMessage()));
        }
    }
}
