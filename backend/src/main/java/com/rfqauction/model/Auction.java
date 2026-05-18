package com.rfqauction.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "auctions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Auction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "rfq_number", nullable = false, unique = true, length = 50)
    private String rfqNumber;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuctionStatus status = AuctionStatus.ACTIVE;

    // ─── Timing ──────────────────────────────────────────────────────────────
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "scheduled_close_time", nullable = false)
    private LocalDateTime scheduledCloseTime;

    @Column(name = "hard_close_time", nullable = false)
    private LocalDateTime hardCloseTime;

    // ─── Extension Configuration ──────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(name = "extension_type", nullable = false)
    @Builder.Default
    private ExtensionType extensionType = ExtensionType.TIME_BASED;

    @Column(name = "extension_trigger_mins", nullable = false)
    @Builder.Default
    private Integer extensionTriggerMins = 5;

    @Column(name = "extension_duration_mins", nullable = false)
    @Builder.Default
    private Integer extensionDurationMins = 5;

    // ─── Item Details ─────────────────────────────────────────────────────────
    @Column(name = "item_name", length = 300)
    private String itemName;

    @Column(precision = 15, scale = 3)
    private BigDecimal quantity;

    @Column(length = 50)
    private String unit;

    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null)  createdAt  = LocalDateTime.now();
        if (updatedAt == null)  updatedAt  = LocalDateTime.now();
        if (startTime == null)  startTime  = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum AuctionStatus { ACTIVE, CLOSED, CANCELLED }

    public enum ExtensionType { TIME_BASED, RANK_BASED, COMBINED }
}
