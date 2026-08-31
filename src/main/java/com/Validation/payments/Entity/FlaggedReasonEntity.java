package com.Validation.payments.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "flagged_reason")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FlaggedReasonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "riskScoreId", nullable = false)
    private Integer riskScoreId;

    @Column(name = "ruleTriggered", nullable = false)
    private String ruleTriggered; // e.g. AMOUNT_DEVIATION, VELOCITY_BREACH, MERCHANT_MISMATCH

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;
}
