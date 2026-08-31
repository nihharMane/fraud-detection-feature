package com.Validation.payments.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "merchant_payment_request")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MerchantPaymentRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "endUserID")
    private String endUserID;

    @Column(name = "merchantTxnReference", nullable = false, unique = true)
    private String merchantTxnReference;

    @Column(name = "transactionRequest", columnDefinition = "TEXT")
    private String transactionRequest;

    @Column(name = "creationDate", nullable = false, updatable = false)
    private LocalDateTime creationDate;
}