package com.rfqauction.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids",
        indexes = {
            @Index(name = "idx_bid_auction",   columnList = "auction_id"),
            @Index(name = "idx_bid_supplier",  columnList = "supplier_id, auction_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "supplier_id", nullable = false)
    private User supplier;

    @Column(name = "bid_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal bidAmount;

    @Column(length = 500)
    private String remarks;

    @Column(name = "bid_time", nullable = false)
    private LocalDateTime bidTime;

    @Column(name = "is_latest", nullable = false)
    @Builder.Default
    private Boolean isLatest = true;

    @Column(name = "rank_at_submit")
    private Integer rankAtSubmit;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (bidTime == null)   bidTime   = LocalDateTime.now();
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
