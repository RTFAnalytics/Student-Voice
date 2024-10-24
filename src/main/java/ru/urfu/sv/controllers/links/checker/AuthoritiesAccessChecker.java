package ru.urfu.sv.controllers.links.checker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import ru.urfu.sv.model.domain.entity.Authority;
import ru.urfu.sv.model.repository.AuthorityRepository;

import static java.util.Objects.nonNull;
import static ru.urfu.sv.model.domain.dto.auth.Roles.ROLE_ADMIN;
import static ru.urfu.sv.model.domain.dto.auth.Roles.ROLE_PROFESSOR;

@Component("AuthoritiesAC")
public class AuthoritiesAccessChecker {

    @Autowired
    private AuthorityRepository authorityRepository;

    public boolean isAdmin() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Current user not authenticated");
        }

        String username = null;
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            username = ((User) authentication.getPrincipal()).getUsername();
        } else {
            username = (String) authentication.getPrincipal();
        }

        final Authority authority = authorityRepository.findAuthorityByUsername(username);

        return (nonNull(username) && authority.getAuthority().equals(ROLE_ADMIN.getName()));
    }

    public boolean isProfessor() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Current user not authenticated");
        }

        String username;
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            username = ((User) authentication.getPrincipal()).getUsername();
        } else {
            username = (String) authentication.getPrincipal();
        }

        final Authority authority = authorityRepository.findAuthorityByUsername(username);

        return (nonNull(username) && authority.getAuthority().equals(ROLE_PROFESSOR.getName()));
    }

    public boolean isAdminOrProfessor() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Current user not authenticated");
        }

        String username;
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            username = ((User) authentication.getPrincipal()).getUsername();
        } else {
            username = (String) authentication.getPrincipal();
        }

        final Authority authority = authorityRepository.findAuthorityByUsername(username);

        return (nonNull(username) && (authority.getAuthority().equals(ROLE_ADMIN.getName()) || authority.getAuthority().equals(ROLE_PROFESSOR.getName())));
    }
}