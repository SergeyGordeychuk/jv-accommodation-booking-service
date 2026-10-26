package com.accommodation.bookingservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity
@Table(name = "accommodations")
@Data
@SQLDelete(sql = "UPDATE accommodations SET is_deleted = true WHERE id = ?")
@Where(clause = "is_deleted = false")
public class Accommodation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Type type;

    @Column(nullable = false, length = 500)
    private String location;

    @Column(nullable = false, length = 100)
    private String size;

    @Column(columnDefinition = "TEXT")
    private String amenities;

    @Column(name = "daily_rate", nullable = false, precision = 10, scale = 2)
    private BigDecimal dailyRate;

    @Column(nullable = false)
    private Integer availability;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public enum Type {
        HOUSE,
        APARTMENT,
        CONDO,
        VACATION_HOME
    }
}
