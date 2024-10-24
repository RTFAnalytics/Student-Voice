package ru.urfu.sv.controllers.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.urfu.sv.controllers.UsersController;
import ru.urfu.sv.utils.result.ActionResultResponse;

import java.io.IOException;
import java.util.Map;

import static ru.urfu.sv.utils.consts.Parameters.*;
import static ru.urfu.sv.utils.model.ModelUtils.orNull;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@PreAuthorize("@AuthoritiesAC.isAdmin()")
public class UsersApiController {
    private final UsersController usersController;

    @PostMapping("create-from-file")
    @Parameters(value = {
            @Parameter(name = "file", required = true)
    })
    public ResponseEntity<Map<String, Object>> uploadUsers(@RequestParam("file") MultipartFile file) throws IOException {
        ExtendedModelMap model = new ExtendedModelMap();
        usersController.uploadUsers(file, model);

        ActionResultResponse result = ActionResultResponse.fromActionResult(model.getAttribute(RESULT));
        if (!result.isSuccess()) {
            return ResponseEntity.ok().body(Map.of(RESULT, result));
        }

        String encodedFile = (String) model.getAttribute(CREATED_USERS);
        return ResponseEntity.ok().body(Map.of(RESULT, result, "createdUsersFile", orNull(encodedFile)));
    }

    @PostMapping("create")
    @Parameters(value = {
            @Parameter(name = "username", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "password", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "role", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "professorName", in = ParameterIn.QUERY)
    })
    public ResponseEntity<Map<String, Object>> createUser(HttpServletRequest request) {
        ExtendedModelMap model = new ExtendedModelMap();
        usersController.createUser(request, model);

        ActionResultResponse result = ActionResultResponse.fromActionResult(model.getAttribute(RESULT));
        return ResponseEntity.ok().body(Map.of(RESULT, result));
    }

    @GetMapping("list")
    public ResponseEntity<Map<String, Object>> usersList(HttpServletRequest request) {
        ExtendedModelMap model = new ExtendedModelMap();
        usersController.usersPage(request, model);
        return ResponseEntity.ok().body(Map.of("users", orNull(model.getAttribute("users"))));
    }

    @PostMapping("update")
    @Parameters(value = {
            @Parameter(name = "username", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "password", in = ParameterIn.QUERY),
            @Parameter(name = "professorName", in = ParameterIn.QUERY),
    })
    public ResponseEntity<Map<String, Object>> updateUser(HttpServletRequest request) {
        ExtendedModelMap model = new ExtendedModelMap();
        usersController.updateUser(request, model);

        ActionResultResponse result = ActionResultResponse.fromActionResult(model.getAttribute(RESULT));
        return ResponseEntity.ok().body(Map.of(RESULT, result));
    }

    @PostMapping("delete")
    @Parameters(value = {
            @Parameter(name = "username", in = ParameterIn.QUERY, required = true)
    })
    public ResponseEntity<Map<String, Object>> deleteUser(HttpServletRequest request) {
        ExtendedModelMap model = new ExtendedModelMap();
        usersController.deleteUser(request.getParameter(USERNAME), model);

        ActionResultResponse result = ActionResultResponse.fromActionResult(model.getAttribute(RESULT));
        return ResponseEntity.ok().body(Map.of(RESULT, result));
    }
}
