package ru.urfu.sv.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.urfu.sv.model.domain.entity.UserInfo;
import ru.urfu.sv.services.CreateUserFromFileService;
import ru.urfu.sv.services.ProfessorService;
import ru.urfu.sv.services.UserService;
import ru.urfu.sv.utils.consts.Parameters;
import ru.urfu.sv.utils.consts.Roles;
import ru.urfu.sv.utils.formatters.TemporalFormatter;
import ru.urfu.sv.utils.result.ActionResult;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

import static ru.urfu.sv.utils.consts.Parameters.*;
import static ru.urfu.sv.utils.consts.Roles.ADMIN;
import static ru.urfu.sv.utils.consts.Roles.PROFESSOR;
import static ru.urfu.sv.utils.consts.Templates.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/admin/users")
public class UsersController {
    private final UserService userService;
    private final CreateUserFromFileService createUsersFromFileService;
    private final ProfessorService professorService;

    @GetMapping("create")
    public String createUserPage(HttpServletRequest request, Model model) {
        model.addAttribute("roles", List.of(ADMIN, PROFESSOR));

        log.info("user create page requested from {}", request.getRemoteUser());
        return CREATE_USER;
    }

    @GetMapping("create-from-file")
    public String createFromFilePage(){
        return CREATE_USERS_FROM_FILE;
    }

    @PostMapping("create-from-file")
    public String uploadUsers(@RequestParam("file") MultipartFile file, Model model) throws IOException {
        if(file.isEmpty()){
            model.addAttribute(RESULT, new ActionResult(false, "Файл не должен быть пустым"));
            return CREATE_USERS_FROM_FILE;
        }
        ActionResult result = createUsersFromFileService.createUsersFromFile(file.getInputStream());

        if(!result.isSuccess()){
            model.addAttribute(RESULT, result);
            return CREATE_USERS_FROM_FILE;
        }

        model.addAttribute(RESULT, new ActionResult(true, "Пользователи успешно созданы"));
        model.addAttribute(CURRENT_DATE, TemporalFormatter.instantToEkbDateTime(Instant.now()));
        String encodedUsers = Base64.getEncoder().encodeToString(result.getMessage().getBytes());
        model.addAttribute(CREATED_USERS, encodedUsers);
        return CREATE_USERS_FROM_FILE;
    }

    @PostMapping("create")
    public String createUser(HttpServletRequest request, Model model) {
        log.info("user create command sent from {}", request.getRemoteUser());

        return userCreateTemplate(request, model);
    }

    @GetMapping("list")
    public String usersPage(HttpServletRequest request, Model model) {
        model.addAttribute("users", userService.findAllUsers());
        log.info("users page requested from {}", request.getRemoteUser());
        return USERS;
    }

    @GetMapping("update/{username}")
    public String updateUserPage(@PathVariable("username") String username, HttpServletRequest request, Model model) {
        log.info("update user page requested from {}", request.getRemoteUser());

        Optional<UserInfo> user = userService.findUserByUsername(username);
        if (user.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.userNotExist(username));
            return UPDATE_USER;
        }

        model.addAttribute("user", user.get());
        return UPDATE_USER;
    }

    @PostMapping("update")
    public String updateUser(HttpServletRequest request, Model model) {
        String username = request.getParameter(USERNAME);
        Optional<UserInfo> userInfo = userService.findUserByUsername(username);
        if (userInfo.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.userNotExist(username));
            return UPDATE_USER;
        }
        String password = request.getParameter(PASSWORD);
        String professorName = request.getParameter(PROFESSOR_NAME);
        ActionResult result = userService.updateUser(username, UserInfo
                .builder()
                .username(username)
                .password(password)
                .additionalData(professorName.isBlank() ? null : Map.of(PROFESSOR_NAME, professorName))
                .build());
        model.addAttribute(RESULT, result);
        model.addAttribute("users", userService.findAllUsers());
        return USERS;
    }

    @PostMapping("delete/{username}")
    public String deleteUser(@PathVariable("username") String username, Model model) {
        Optional<UserInfo> userInfo = userService.findUserByUsername(username);
        if (userInfo.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.userNotExist(username));
            return UPDATE_USER;
        }
        ActionResult result = userService.deleteUser(username);
        model.addAttribute(RESULT, result);
        model.addAttribute("users", userService.findAllUsers());
        return USERS;
    }

    private String userCreateTemplate(HttpServletRequest request, Model model) {
        String username = request.getParameter(USERNAME);
        String password = request.getParameter(PASSWORD);
        String role = request.getParameter(ROLE);
        Map<String, String> additionalData = getAdditionalData(request);

        UserInfo userInfo = UserInfo.builder()
                .username(username)
                .password(password)
                .role(role)
                .additionalData(additionalData)
                .build();

        ActionResult result = userService.createUser(userInfo);

        if (result.isSuccess() && Roles.PROFESSOR.equals(userInfo.getRole())) {
            result = professorService.createProfessor(username, userInfo.getAdditionalData().get(Parameters.PROFESSOR_NAME));
        }

        model.addAttribute(ROLES, List.of(ADMIN, PROFESSOR));
        model.addAttribute(RESULT, result);
        return CREATE_USER;
    }

    private Map<String, String> getAdditionalData(HttpServletRequest request) {
        return Map.ofEntries(
                Map.entry(PROFESSOR_NAME, request.getParameter(PROFESSOR_NAME))
        );
    }

}
