package com.Validation.payments.security.Filters;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;

public class HmacAuthenticationToken extends AbstractAuthenticationToken {

    private final String userName;
    private final String signature;

    public HmacAuthenticationToken(String userName, String signature, String role) {
        super(AuthorityUtils.createAuthorityList(role));
        this.userName = userName;
        this.signature = signature;

        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return signature;
    }

    @Override
    public Object getPrincipal() {
        return userName;
    }
}