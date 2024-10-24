package ru.urfu.sv.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import ru.urfu.sv.model.domain.entity.Authority;
import ru.urfu.sv.model.repository.AuthorityRepository;

import static java.util.Objects.isNull;

@Service
public class AuthorityService {

    @Autowired
    private AuthorityRepository authorityRepository;

    public Authority findAuthorityForCheck() {
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

        if (isNull(username)) {
            return null;
        }

        return authorityRepository.findAuthorityByUsername(username);
    }
}