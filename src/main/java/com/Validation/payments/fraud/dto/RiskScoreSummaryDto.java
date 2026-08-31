package com.Validation.payments.fraud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskScoreSummaryDto {
    private Integer id;
    private String merchantTxnReference;
    private String endUserID;
    private int score;
    private String verdict;
    private boolean overridden;
    private LocalDateTime createdDate;
}