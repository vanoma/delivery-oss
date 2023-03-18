package com.vanoma.api.order.packages;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import java.time.OffsetDateTime;

@Entity(name = "stop_verification")
public class StopVerification {

    @Id
    @Column(name = "stop_verification_id", nullable = false)
    private String stopVerificationId;

    @Column(name = "package_id", nullable = false)
    private String packageId;

    @Column(name = "from_code", nullable = false)
    private String fromCode;

    @Column(name = "from_url", nullable = true)
    private String fromUrl;

    @Column(name = "verify_pick_up",
            columnDefinition = "boolean default false")
    private Boolean verifyPickUp = false;

    @Column(name = "to_code", nullable = false)
    private String toCode;

    @Column(name = "to_url", nullable = false)
    private String toUrl;

    @Column(name = "verify_drop_off",
            columnDefinition = "boolean default true")
    private Boolean verifyDropOff = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    void preInsert() {
        if (this.verifyPickUp == null) this.verifyPickUp = false;
        if (this.verifyDropOff == null) this.verifyDropOff = true;
    }
}
