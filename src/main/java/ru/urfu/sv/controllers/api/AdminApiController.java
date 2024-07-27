package ru.urfu.sv.controllers.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.urfu.sv.controllers.AdminController;
import ru.urfu.sv.utils.result.ActionResultResponse;

import java.util.Map;

import static ru.urfu.sv.utils.consts.Parameters.RESULT;

@RestController
@RequiredArgsConstructor
public class AdminApiController {
    private final AdminController adminController;

    @PostMapping("/api/admin/create-first")
    @Parameters(value = {
            @Parameter(name = "username", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "password", in = ParameterIn.QUERY, required = true)
    })
    public ResponseEntity<Map<String, Object>> createFirstAdmin(HttpServletRequest request) {
        ExtendedModelMap model = new ExtendedModelMap();
        adminController.createFirstAdmin(request, model);
        ActionResultResponse resultResponse = ActionResultResponse.fromActionResult(model.getAttribute(RESULT));
        Map<String, Object> body = Map.of(RESULT, resultResponse);
        return ResponseEntity.ok().body(body);
    }
}
