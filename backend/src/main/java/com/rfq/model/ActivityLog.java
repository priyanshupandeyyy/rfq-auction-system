package com.rfq.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "activity_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rfq_id", nullable = false)
    private Auction auction;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(columnDefinition = "JSON")
    private String metadata;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Common event types as constants
    public static final String EVENT_BID_SUBMITTED = "BID_SUBMITTED";
    public static final String EVENT_AUCTION_EXTENDED = "AUCTION_EXTENDED";
    public static final String EVENT_AUCTION_CLOSED = "AUCTION_CLOSED";
    public static final String EVENT_AUCTION_CREATED = "AUCTION_CREATED";
    public static final String EVENT_AUCTION_ACTIVATED = "AUCTION_ACTIVATED";
    public static final String EVENT_RANK_CHANGED = "RANK_CHANGED";
}
