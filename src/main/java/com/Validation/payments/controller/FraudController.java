package com.Validation.payments.controller;

import com.Validation.payments.fraud.FraudQueryService;
import com.Validation.payments.fraud.dto.OverrideRequestDto;
import com.Validation.payments.fraud.dto.RiskScoreDetailDto;
import com.Validation.payments.fraud.dto.RiskScoreSummaryDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/fraud")
@Slf4j
@RequiredArgsConstructor
public class FraudController {

    private final FraudQueryService fraudQueryService;

    @GetMapping("/flagged")
    public List<RiskScoreSummaryDto> getFlagged(
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") int offset) {

        log.info("Fetching flagged transactions | limit: {}, offset: {}", limit, offset);
        return fraudQueryService.listFlagged(limit, offset);
    }

    @GetMapping("/{merchantTxnReference}")
    public RiskScoreDetailDto getDetail(@PathVariable String merchantTxnReference) {

        log.info("Fetching risk detail for txnRef: {}", merchantTxnReference);
        return fraudQueryService.getDetail(merchantTxnReference);
    }

    @PutMapping("/{merchantTxnReference}/override")
    public void override(@PathVariable String merchantTxnReference,
                         @Valid @RequestBody OverrideRequestDto request) {

        log.info("Overriding risk decision for txnRef: {}", merchantTxnReference);
        fraudQueryService.override(merchantTxnReference, request.getOverrideNote());
    }
}