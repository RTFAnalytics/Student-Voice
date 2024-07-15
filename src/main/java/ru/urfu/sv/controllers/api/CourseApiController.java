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
import ru.urfu.sv.controllers.CourseController;
import ru.urfu.sv.utils.result.ActionResultResponse;

import java.util.Map;
import java.util.UUID;

import static ru.urfu.sv.utils.consts.Parameters.*;
import static ru.urfu.sv.utils.model.ModelUtils.orNull;
import static ru.urfu.sv.utils.result.ActionResultResponse.fromActionResult;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/courses")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
public class CourseApiController {
    private final CourseController courseController;

    @PostMapping("create")
    public ResponseEntity<Map<String, Object>> createCourse(HttpServletRequest request) {
        ExtendedModelMap model = new ExtendedModelMap();
        courseController.createCourse(null, request, model);
        ActionResultResponse result = fromActionResult(model.getAttribute(RESULT));

        return ResponseEntity.ok().body(Map.of(RESULT, result));
    }

    @GetMapping("find")
    public ResponseEntity<Map<String, Object>> findCourse(HttpServletRequest request) {
        ExtendedModelMap model = new ExtendedModelMap();
        courseController.coursePage(UUID.fromString(request.getParameter(COURSE_ID)), model);
        ActionResultResponse result = fromActionResult(model.getAttribute(RESULT));

        return ResponseEntity.ok().body(
                Map.ofEntries(Map.entry(RESULT, result),
                        Map.entry(CLASS_SESSIONS_LIST, orNull(model.getAttribute(CLASS_SESSIONS_LIST))),
                        Map.entry(COURSE_DETAILS, orNull(model.getAttribute(COURSE_DETAILS)))
                ));
    }
}
