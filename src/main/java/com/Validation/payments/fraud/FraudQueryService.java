package com.Validation.payments.fraud;

import com.Validation.payments.Constants.ErrorCode;
import com.Validation.payments.Entity.FlaggedReasonEntity;
import com.Validation.payments.Entity.RiskScoreEntity;
import com.Validation.payments.Exception.PaymentValidationException;
import com.Validation.payments.fraud.dto.RiskScoreDetailDto;
import com.Validation.payments.fraud.dto.RiskScoreSummaryDto;
import com.Validation.payments.repository.interfaces.RiskScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudQueryService {

    private final RiskScoreRepository riskScoreRepository;

    public List<RiskScoreSummaryDto> listFlagged(int limit, int offset) {
        return riskScoreRepository.findFlagged(limit, offset).stream()
                .map(this::toSummary)
                .toList();
    }

    public RiskScoreDetailDto getDetail(String merchantTxnReference) {

        RiskScoreEntity entity = riskScoreRepository.findByMerchantTxnReference(merchantTxnReference)
                .orElseThrow(() -> new PaymentValidationException(
                        ErrorCode.RECORD_NOT_FOUND.getCode(),
                        ErrorCode.RECORD_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND
                ));

        List<FlaggedReasonEntity> reasons = riskScoreRepository.findFlaggedReasonsByRiskScoreId(entity.getId());

        List<RiskScoreDetailDto.FlaggedReasonDto> reasonDtos = reasons.stream()
                .map(r -> new RiskScoreDetailDto.FlaggedReasonDto(r.getRuleTriggered(), r.getDetails()))
                .toList();

        return new RiskScoreDetailDto(
                entity.getId(),
                entity.getMerchantTxnReference(),
                entity.getEndUserID(),
                entity.getScore(),
                entity.getVerdict(),
                entity.getReasonSummary(),
                entity.isOverridden(),
                entity.getOverrideNote(),
                entity.getCreatedDate(),
                reasonDtos
        );
    }

    public void override(String merchantTxnReference, String overrideNote) {
        RiskScoreEntity entity = riskScoreRepository.findByMerchantTxnReference(merchantTxnReference)
                .orElseThrow(() -> new PaymentValidationException(
                        ErrorCode.RECORD_NOT_FOUND.getCode(),
                        ErrorCode.RECORD_NOT_FOUND.getMessage(),
                        HttpStatus.NOT_FOUND
                ));

        riskScoreRepository.markOverridden(entity.getId(), overrideNote);
    }

    private RiskScoreSummaryDto toSummary(RiskScoreEntity entity) {
        return new RiskScoreSummaryDto(
                entity.getId(),
                entity.getMerchantTxnReference(),
                entity.getEndUserID(),
                entity.getScore(),
                entity.getVerdict(),
                entity.isOverridden(),
                entity.getCreatedDate()
        );
    }
}