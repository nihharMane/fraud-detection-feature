package com.Validation.payments.fraud.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RiskScoreDetailDto {
    private Integer id;
    private String merchantTxnReference;
    private String endUserID;
    private int score;
    private String verdict;
    private String reasonSummary;
    private boolean overridden;
    private String overrideNote;
    private LocalDateTime createdDate;
    private List<FlaggedReasonDto> flaggedReasons;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlaggedReasonDto {
        private String ruleTriggered;
        private String details;
    }
}