package com.rfq.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Auction auction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private User supplier;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "rank_position")
    private Integer rankPosition;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "is_latest")
    @Builder.Default
    private Boolean isLatest = true;

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
    }
}
