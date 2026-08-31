package com.Validation.payments.fraud;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Calls an LLM (via Spring AI) to explain WHY a transaction looks risky, in plain
 * language, for cases the rule engine marks as ambiguous (MEDIUM verdict).
 * Falls back to a rule-based summary if the model call fails for any reason -
 * this must never block a real payment from being processed.
 */
@Slf4j
@Service
public class FraudReasoningService {

    private final ChatClient chatClient;

    public FraudReasoningService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String explainRisk(String endUserID, long amount, List<FraudRuleResult> triggeredRules) {

        log.info("========== ENTERED FRAUD REASONING SERVICE ==========");

        String ruleSummary = triggeredRules.stream()
                .map(r -> r.getRuleName() + ": " + r.getDetails())
                .reduce("", (a, b) -> a.isEmpty() ? b : a + "; " + b);

        String prompt = """
            A payment fraud rule engine flagged a transaction as medium risk.
            User: %s
            Amount: %d
            Triggered rules: %s

            In 2-3 plain sentences, explain what pattern this looks like
            (e.g. card testing, account takeover, one-off large purchase)
            and how confident an ops reviewer should be that this needs
            manual review.
            Do not repeat the raw rule names verbatim.
            """.formatted(endUserID, amount, ruleSummary);

        log.info("Prompt Sent:\n{}", prompt);

        try {

            log.info("Calling Groq AI...");

            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.info("========== AI RESPONSE ==========");
            log.info(response);

            return response;

        } catch (Exception ex) {

            log.error("========== AI ERROR ==========", ex);

            return "Automated reasoning unavailable. Triggered rules: " + ruleSummary;
        }
    }
}
