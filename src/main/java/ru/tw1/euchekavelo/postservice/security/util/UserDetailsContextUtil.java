package ru.tw1.euchekavelo.postservice.security.util;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class UserDetailsContextUtil {

    public List<String> getRoles() {
        return SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
    }

    public UUID getUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof Jwt) {
            return UUID.fromString(((Jwt) principal).getClaimAsString("user_id"));
        } else if (principal instanceof OidcUser) {
            return UUID.fromString(Objects.requireNonNull(((OidcUser) principal).getAttribute("user_id")));
        }

        return null;
    }
}