package ru.urfu.sv.controllers.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.urfu.sv.controllers.ProfessorController;
import ru.urfu.sv.model.domain.ClassSession;
import ru.urfu.sv.model.domain.Professor;
import ru.urfu.sv.services.ClassSessionService;
import ru.urfu.sv.services.CourseService;
import ru.urfu.sv.services.ProfessorService;
import ru.urfu.sv.utils.exceptions.ModeusException;
import ru.urfu.sv.utils.result.ActionResultFactory;
import ru.urfu.sv.utils.result.ActionResultResponse;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.urfu.sv.utils.consts.Parameters.*;
import static ru.urfu.sv.utils.consts.Parameters.CLASS_SESSIONS_LIST;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/professors")
@PreAuthorize("hasRole('PROFESSOR')")
public class ProfessorApiController {
    private final ProfessorService professorService;
    private final CourseService courseService;
    private final ClassSessionService sessionService;

    @GetMapping("current")
    @Parameters(value = {
            @Parameter(name = "from", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "to", in = ParameterIn.QUERY, required = true)
    })
    public ResponseEntity<Map<String, Object>> getCurrent(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        String username = userDetails.getUsername();
        Optional<Professor> professorOpt = professorService.findProfessorByUsername(username);

        if (professorOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(RESULT, ActionResultResponse.fromActionResult(ActionResultFactory.professorNotExist(username))));
        } else {
            String professorName = professorOpt.get().getFullName();
            LocalDate from = LocalDate.parse(request.getParameter("from"));
            LocalDate to = LocalDate.parse(request.getParameter("to"));

            return ResponseEntity.ok().body(
                    Map.ofEntries(
                            Map.entry(PROFESSOR_NAME, professorOpt.get().getFullName()),
                            Map.entry(COURSES_LIST, courseService.findCoursesByProfessorName(professorName)),
                            Map.entry(CLASS_SESSIONS_LIST, sessionService.findSavedClassSessionsByProfessorName(professorName, from, to))
                    )
            );
        }
    }

    @PostMapping("update-sessions")
    @Parameters(value = {
            @Parameter(name = "from", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "to", in = ParameterIn.QUERY, required = true)
    })
    public ResponseEntity<Map<String, Object>> update(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {
        String username = userDetails.getUsername();
        Optional<Professor> professorOpt = professorService.findProfessorByUsername(username);

        if (professorOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(RESULT, ActionResultResponse.fromActionResult(ActionResultFactory.professorNotExist(username))));
        } else {
            String professorName = professorOpt.get().getFullName();
            LocalDate from = LocalDate.parse(request.getParameter("from"));
            LocalDate to = LocalDate.parse(request.getParameter("to"));

            try {
                List<ClassSession> allSessions = sessionService.findAllClassSessionsByProfessorName(professorName, from, to);
                return ResponseEntity.ok(Map.of(CLASS_SESSIONS_LIST, allSessions, PROFESSOR_NAME, professorName));
            } catch (ModeusException e) {
                return ResponseEntity.ok(Map.of(RESULT, new ActionResultResponse(false, "Во время получения пар из модеуса произошла ошибка")));
            }
        }

    }
}
