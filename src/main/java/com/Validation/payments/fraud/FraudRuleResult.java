package com.Validation.payments.fraud;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FraudRuleResult {

    private String ruleName;
    private boolean triggered;
    private int points;
    private String details;

    public static FraudRuleResult notTriggered(String ruleName) {
        return new FraudRuleResult(ruleName, false, 0, null);
    }
}
