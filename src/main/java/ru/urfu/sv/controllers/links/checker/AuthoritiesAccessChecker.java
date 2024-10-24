package ru.urfu.sv.controllers.links.checker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.urfu.sv.model.domain.entity.Authority;
import ru.urfu.sv.services.AuthorityService;

import static ru.urfu.sv.model.domain.dto.auth.Roles.ROLE_ADMIN;
import static ru.urfu.sv.model.domain.dto.auth.Roles.ROLE_PROFESSOR;

@Component("AuthoritiesAC")
public class AuthoritiesAccessChecker {

    @Autowired
    private AuthorityService authorityService;

    public boolean isAdmin() {
        final Authority authority = authorityService.findAuthorityForCheck();
        return authority.getAuthority().equals(ROLE_ADMIN.getName());
    }

    public boolean isProfessor() {
        final Authority authority = authorityService.findAuthorityForCheck();
        return authority.getAuthority().equals(ROLE_PROFESSOR.getName());
    }

    public boolean isAdminOrProfessor() {
        final Authority authority = authorityService.findAuthorityForCheck();
        return authority.getAuthority().equals(ROLE_ADMIN.getName()) || authority.getAuthority().equals(ROLE_PROFESSOR.getName());
    }
}