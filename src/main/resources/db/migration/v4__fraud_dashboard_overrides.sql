-- Add override tracking to risk_score, needed by the fraud dashboard's override action --

ALTER TABLE validations.risk_score
ADD COLUMN overridden BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN override_note VARCHAR(500) DEFAULT NULL;