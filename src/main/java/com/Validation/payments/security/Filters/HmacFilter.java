package com.Validation.payments.security.Filters;

import com.Validation.payments.Constants.Constant;
import com.Validation.payments.util.HmacSHA256Util;
import com.Validation.payments.util.JsonUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

import static com.Validation.payments.Constants.Constant.ROLE_MERCHANT;

@Slf4j
@RequiredArgsConstructor
public class HmacFilter extends OncePerRequestFilter {

    private final HmacSHA256Util hmacSHA256Util;
    private final JsonUtil jsonUtil;

    // Skip HMAC validation for health endpoint
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getServletPath().equals("/v1/payments/health");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        CachedBodyHttpServletRequest wrappedRequest =
                new CachedBodyHttpServletRequest(request);

        String body = wrappedRequest.getBody();

        String headerSignature = request.getHeader("Hmac-Signature");

        log.info("Header HMAC: {}", headerSignature);
        log.info("Body: {}", body);

        if (body == null || body.isEmpty()) {
            throw new RuntimeException("Request body is empty");
        }

        String calculatedHmac = hmacSHA256Util.generateHmac(body);

        log.info("Calculated HMAC: {}", calculatedHmac);

        if (headerSignature == null || !calculatedHmac.equals(headerSignature)) {
            log.error("Invalid HMAC Signature");
            throw new AccessDeniedException("Invalid HMAC Signature");
        }

        log.info("HMAC Signature validated");

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Authentication authentication =
                new HmacAuthenticationToken(
                        Constant.MERCHANT_ID,
                        headerSignature,
                        ROLE_MERCHANT
                );

        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        filterChain.doFilter(wrappedRequest, response);
    }
}