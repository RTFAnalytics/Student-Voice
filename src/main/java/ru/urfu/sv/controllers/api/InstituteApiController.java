package ru.urfu.sv.controllers.api;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.urfu.sv.controllers.InstituteController;
import ru.urfu.sv.model.domain.Institute;
import ru.urfu.sv.services.InstituteService;
import ru.urfu.sv.utils.result.ActionResultResponse;

import java.util.List;
import java.util.Map;

import static ru.urfu.sv.utils.consts.Parameters.INSTITUTES_LIST;
import static ru.urfu.sv.utils.consts.Parameters.RESULT;
import static ru.urfu.sv.utils.result.ActionResultResponse.fromActionResult;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/institutes")
@PreAuthorize("hasRole('ADMIN')")
public class InstituteApiController {
    private final InstituteController instituteController;
    private final InstituteService instituteService;

    @PostMapping("create")
    public ResponseEntity<Map<String, Object>> create(HttpServletRequest request) {
        ExtendedModelMap model = new ExtendedModelMap();
        instituteController.createInstitute(request, model);

        ActionResultResponse result = fromActionResult(model.getAttribute(RESULT));
        return ResponseEntity.ok().body(Map.of(RESULT, result));
    }

    @GetMapping("list")
    public ResponseEntity<Map<String, Object>> list() {
        List<Institute> institutes = instituteService.findAllInstitutes();
        return ResponseEntity.ok().body(Map.of(INSTITUTES_LIST, institutes));
    }
}
