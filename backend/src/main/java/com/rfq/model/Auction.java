package com.rfq.model;

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

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id", nullable = false)
    private User buyer;

    @Column(name = "base_price", precision = 15, scale = 2)
    private BigDecimal basePrice;

    @Column(length = 10)
    @Builder.Default
    private String currency = "INR";

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    /**
     * Hard close time — auction NEVER extends beyond this timestamp.
     */
    @Column(name = "hard_close_time", nullable = false)
    private LocalDateTime hardCloseTime;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "extension_trigger")
    @Builder.Default
    private ExtensionTrigger extensionTrigger = ExtensionTrigger.TIME;

    /**
     * How many minutes to add to end_time when extension is triggered.
     */
    @Column(name = "extension_minutes")
    @Builder.Default
    private Integer extensionMinutes = 5;

    /**
     * If a bid is placed within this many minutes of end_time, trigger extension.
     */
    @Column(name = "extension_window_minutes")
    @Builder.Default
    private Integer extensionWindowMinutes = 3;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum Status {
        DRAFT, ACTIVE, CLOSED, CANCELLED
    }

    public enum ExtensionTrigger {
        TIME, RANK, COMBINED
    }
}
