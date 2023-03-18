package com.vanoma.api.order.packages;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "cash_collection",
        indexes = {
                @Index(name = "cash_collection_package_id_idx", columnList = "package_id", unique = true)
        })
public class CashCollection {

    @Id
    @Column(name = "cash_collection_id", nullable = false)
    private String cashCollectionId;

    @Column(name = "package_id", nullable = false)
    private String packageId;

    @Column(name = "collection_amount",
            precision = 10, scale = 2,
            nullable = false)
    private BigDecimal collectionAmount;

    @Column(name = "collected_amount",
            precision = 10, scale = 2,
            nullable = false)
    private BigDecimal collectedAmount;

    @Column(name = "note", nullable = true)
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}
