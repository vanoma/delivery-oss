package com.vanoma.api.order.contacts;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "address_picture",
        indexes = {
                @Index(name = "address_picture_address_id_idx", columnList = "address_id", unique = true),
                @Index(name = "address_picture_house_number_street_name_idx", columnList = "house_number,street_name", unique = false)

        })
public class AddressPicture {

    @Id
    @Column(name = "address_picture_id", nullable = false)
    private String addressPictureId;

    @Column(name = "address_id", unique = true, nullable = false)
    private String addressId;

    @Column(name = "house_number", nullable = true)
    private String houseNumber;

    @Column(name = "street_name", nullable = true)
    private String streetName;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}