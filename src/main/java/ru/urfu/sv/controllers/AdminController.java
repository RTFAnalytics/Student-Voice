package ru.urfu.sv.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.homyakin.iuliia.Schemas;
import ru.homyakin.iuliia.Translator;
import ru.urfu.sv.services.InstituteService;
import ru.urfu.sv.utils.PasswordGenerator;
import ru.urfu.sv.utils.consts.AdminPaths;
import ru.urfu.sv.utils.consts.Templates;
import ru.urfu.sv.utils.model.CreatedUser;
import ru.urfu.sv.utils.result.ActionResult;
import ru.urfu.sv.utils.consts.Roles;
import ru.urfu.sv.model.domain.UserInfo;
import ru.urfu.sv.services.UserService;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.urfu.sv.utils.consts.Parameters.*;
import static ru.urfu.sv.utils.consts.Templates.*;

@Controller
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = {"/admin", "/admin-home"})
    public String adminHome(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("adminPaths", AdminPaths.getPaths());
        model.addAttribute(USERNAME, userDetails.getUsername());
        return ADMIN_HOME;
    }

    @GetMapping("/admin/create-first")
    public String createFirstAdminPage(Model model) {
        if (userService.isAnyAdminExists()) {
            model.addAttribute(RESULT, ActionResultFactory.firstAdminExist());
        }
        return CREATE_FIRST_ADMIN;
    }

    @PostMapping("/admin/create-first")
    public String createFirstAdmin(HttpServletRequest request, Model model) {
        if (userService.isAnyAdminExists()) {
            model.addAttribute(RESULT, ActionResultFactory.firstAdminExist());
            return CREATE_FIRST_ADMIN;
        }

        UserInfo userInfo = UserInfo.builder()
                .username(request.getParameter(USERNAME))
                .password(request.getParameter(PASSWORD))
                .role(Roles.ADMIN)
                .build();

        ActionResult result = userService.createUser(userInfo);

        model.addAttribute(RESULT, result);
        return CREATE_FIRST_ADMIN;
    }


}
