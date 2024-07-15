package ru.urfu.sv.utils.consts;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Roles {
    public static final String ADMIN = "ADMIN";
    public static final String PROFESSOR = "PROFESSOR";
    public static final String ANON = "ANON";

    public static final List<String> ALL_ROLES = List.of(ADMIN, PROFESSOR);

    public static String getRoleFromAuthorities(Collection<? extends GrantedAuthority> authorities) {
        for (String role : ALL_ROLES) {
            if (authorities.contains(new SimpleGrantedAuthority("ROLE_".concat(role)))) {
                return role;
            }
        }
        return ANON;
    }
}
