package ru.urfu.sv.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.urfu.sv.utils.consts.Roles;
import ru.urfu.sv.utils.consts.Templates;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {
    private final PasswordEncoder passwordEncoder;

    @GetMapping(value = {"/", "home"})
    public String home(Model model, HttpServletRequest request) {
        String username = StringUtils.hasText(request.getRemoteUser()) ? request.getRemoteUser() : Roles.ANON;
        model.addAttribute("username", username);
        return Templates.HOME;
    }

    @GetMapping("encode")
    @ResponseBody
    public String encode(@RequestParam("password") String password) {
        return passwordEncoder.encode(password);
    }
}
