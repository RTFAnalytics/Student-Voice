package ru.urfu.sv.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import ru.urfu.sv.utils.consts.Roles;

import java.util.Map;

import static ru.urfu.sv.utils.consts.Templates.*;

@Controller
@Slf4j
public class LoginController {
    private static final Map<String, String> homeTemplates = Map.ofEntries(
            Map.entry(Roles.ADMIN, ADMIN_HOME),
            Map.entry(Roles.PROFESSOR, PROFESSOR_HOME)
    );

    @GetMapping("/login-page")
    public String login(HttpServletRequest request) {
        log.info("login requested from {}", request.getRemoteAddr());
        return LOGIN_PAGE;
    }

    @PostMapping("/user-home")
    public String userHome(@AuthenticationPrincipal UserDetails userDetails) {
        String template = homeTemplates.get(Roles.getRoleFromAuthorities(userDetails.getAuthorities()));

        if (template == null) {
            log.warn("there is no home page for user {}", userDetails);
            return HOME;
        }

        log.info("redirect user {} to his home {}", userDetails.getUsername(), template);
        return REDIRECT.concat(template);
    }

}
