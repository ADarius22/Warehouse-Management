package com.darius.wms.security;

import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/** Access to the user of the current request (set by Spring Security from the JWT). */
public final class CurrentUser {

    private static final String ROLE_PREFIX = "ROLE_";

    private CurrentUser() {
    }

    /** Username of the authenticated user, or {@code null} outside a request (e.g. migrations, jobs). */
    public static String username() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null || !auth.isAuthenticated() ? null : auth.getName();
    }

    /**
     * Role names ("ADMIN", "OPERATOR") of an authentication. Other authorities are ignored, e.g. the
     * "FACTOR_PASSWORD" / "FACTOR_BEARER" authorities Spring Security 7 adds to record how the user logged in.
     */
    public static List<String> roles(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith(ROLE_PREFIX))
                .map(a -> a.substring(ROLE_PREFIX.length()))
                .toList();
    }
}
