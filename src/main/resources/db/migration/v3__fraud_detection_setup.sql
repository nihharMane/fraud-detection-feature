-- Create Tables for Fraud Detection Start --

CREATE TABLE validations.`risk_score` (
 `id` int NOT NULL AUTO_INCREMENT,
 `merchantTxnReference` varchar(100) NOT NULL,
 `endUserID` varchar(100) NOT NULL,
 `score` int NOT NULL,
 `verdict` varchar(20) NOT NULL,
 `reasonSummary` text DEFAULT NULL,
 `createdDate` timestamp(2) NOT NULL DEFAULT CURRENT_TIMESTAMP(2),
 PRIMARY KEY (`id`),
 KEY (`merchantTxnReference`),
 KEY (`endUserID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE validations.`flagged_reason` (
 `id` int NOT NULL AUTO_INCREMENT,
 `riskScoreId` int NOT NULL,
 `ruleTriggered` varchar(50) NOT NULL,
 `details` text DEFAULT NULL,
 PRIMARY KEY (`id`),
 FOREIGN KEY (`riskScoreId`) REFERENCES `risk_score` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE validations.`user_transaction_stats` (
 `endUserID` varchar(100) NOT NULL,
 `avgAmount` double NOT NULL DEFAULT 0,
 `stdDevAmount` double NOT NULL DEFAULT 0,
 `transactionCount` bigint NOT NULL DEFAULT 0,
 `lastTransactionAt` timestamp(2) NULL DEFAULT NULL,
 `knownMerchantCategories` varchar(500) DEFAULT NULL,
 PRIMARY KEY (`endUserID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Register the new validator so it can be toggled the same way as existing rules
INSERT INTO validations.validation_rules (validatorName, isActive, priority)
VALUES ('Fraud_Check_Validator', true, 20);

-- Create Tables for Fraud Detection End --
