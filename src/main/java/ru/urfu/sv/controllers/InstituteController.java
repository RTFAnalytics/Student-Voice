package ru.urfu.sv.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.urfu.sv.services.InstituteService;
import ru.urfu.sv.utils.result.ActionResult;

import static ru.urfu.sv.utils.consts.Parameters.*;
import static ru.urfu.sv.utils.consts.Templates.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/institutes")
@PreAuthorize("@AuthoritiesAC.isAdmin()")
public class InstituteController {
    private final InstituteService instituteService;

    @GetMapping("/create")
    public String createInstitutePage() {
        return CREATE_INSTITUTE;
    }

    @PostMapping("/create")
    public String createInstitute(HttpServletRequest request, Model model) {
        String instituteFullName = request.getParameter(INSTITUTE_FULL_NAME);
        String instituteShortName = request.getParameter(INSTITUTE_SHORT_NAME);
        String instituteAddress = request.getParameter(INSTITUTE_ADDRESS);

        ActionResult result = instituteService.createInstitute(instituteFullName, instituteShortName, instituteAddress);
        model.addAttribute(RESULT, result);
        return CREATE_INSTITUTE;
    }
}
