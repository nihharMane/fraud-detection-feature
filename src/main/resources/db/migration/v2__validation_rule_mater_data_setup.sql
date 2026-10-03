-- MUST have priority 0
INSERT INTO `validation_rules` (validatorName, isActive, priority)
VALUES ('DUPLICATION_TXN_RULE', true, 0);

-- All business validations priority 10
INSERT INTO `validation_rules` (validatorName, isActive, priority)
VALUES ('PAYMENT_ATTEMPT_THRESHOLD_RULE', true, 10);

-- Validation Rules Param Configuration
INSERT INTO `validation_rules_params`
(validatorName, paramName, paramValue)
VALUES
    ('PAYMENT_ATTEMPT_THRESHOLD_RULE', 'durationInMins', '2');

INSERT INTO `validation_rules_params`
(validatorName, paramName, paramValue)
VALUES
    ('PAYMENT_ATTEMPT_THRESHOLD_RULE', 'maxPaymentThreshold', '5');