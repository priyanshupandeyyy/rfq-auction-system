package com.rfq.controller;

import com.rfq.dto.ApiResponse;
import com.rfq.dto.BidResponse;
import com.rfq.dto.RankingResponse;
import com.rfq.dto.SubmitBidRequest;
import com.rfq.service.ActivityLogService;
import com.rfq.service.BidService;
import com.rfq.dto.ActivityLogResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;
    private final ActivityLogService activityLogService;

    /** POST /api/bid/submit */
    @PostMapping("/api/bid/submit")
    public ResponseEntity<ApiResponse<BidResponse>> submitBid(@Valid @RequestBody SubmitBidRequest req) {
        BidResponse response = bidService.submitBid(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bid submitted successfully", response));
    }

    /** GET /api/bid/:rfqId */
    @GetMapping("/api/bid/{rfqId}")
    public ResponseEntity<ApiResponse<List<BidResponse>>> getBids(@PathVariable Long rfqId) {
        return ResponseEntity.ok(ApiResponse.success(bidService.getBidsForAuction(rfqId)));
    }

    /** GET /api/ranking/:rfqId */
    @GetMapping("/api/ranking/{rfqId}")
    public ResponseEntity<ApiResponse<RankingResponse>> getRankings(@PathVariable Long rfqId) {
        return ResponseEntity.ok(ApiResponse.success(bidService.getRankings(rfqId)));
    }

    /** GET /api/activity/:rfqId */
    @GetMapping("/api/activity/{rfqId}")
    public ResponseEntity<ApiResponse<List<ActivityLogResponse>>> getActivity(@PathVariable Long rfqId) {
        return ResponseEntity.ok(ApiResponse.success(activityLogService.getActivityForAuction(rfqId)));
    }
}
