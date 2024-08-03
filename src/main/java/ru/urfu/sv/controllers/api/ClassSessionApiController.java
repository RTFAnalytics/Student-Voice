package ru.urfu.sv.controllers.api;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.urfu.sv.controllers.ClassSessionController;
import ru.urfu.sv.model.domain.ClassSession;
import ru.urfu.sv.services.ClassSessionService;
import ru.urfu.sv.utils.formatters.TemporalFormatter;
import ru.urfu.sv.utils.result.ActionResultFactory;
import ru.urfu.sv.utils.result.ActionResultResponse;

import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static ru.urfu.sv.utils.consts.Parameters.*;
import static ru.urfu.sv.utils.model.ModelUtils.orNull;
import static ru.urfu.sv.utils.result.ActionResultResponse.fromActionResult;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sessions")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
public class ClassSessionApiController {
    private final ClassSessionController sessionController;
    private final ClassSessionService sessionService;

    @PostMapping("create")
    @Parameters(value = {
            @Parameter(name = "courseId", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "professorName", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "startSession", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "endSession", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "roomName", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "sessionName", in = ParameterIn.QUERY, required = true)
    })
    public ResponseEntity<Map<String, Object>> createSession(HttpServletRequest request) {
        ExtendedModelMap model = new ExtendedModelMap();
        sessionController.createSession(null, request, model);

        ActionResultResponse result = fromActionResult(model.getAttribute(RESULT));
        return ResponseEntity.ok().body(Map.of(RESULT, result));
    }

    @GetMapping("find")
    @Parameters(value = {
            @Parameter(name = "sessionId", in = ParameterIn.QUERY, required = true)
    })
    public ResponseEntity<Map<String, Object>> findSession(HttpServletRequest request) {
        ExtendedModelMap model = new ExtendedModelMap();
        sessionController.sessionPage(request.getParameter(CLASS_SESSION_ID), model);

        Map<String, Object> timerInfo = null;
        if (model.getAttribute(CLASS_SESSION) != null) {
            ClassSession session = (ClassSession) model.getAttribute(CLASS_SESSION);
            if (session != null && session.getDisableAfterTimestamp() != null) {
                if (Instant.now().isAfter(session.getDisableAfterTimestamp())) {
                    timerInfo = Map.of("isActive", false, "message", "Время действия кода для отзыва вышло");
                } else {
                    //Костыль для зоны UTC+5
                    OffsetDateTime disableTime = session.getDisableAfterTimestamp().atOffset(ZoneOffset.ofHours(5));
                    String message = "Таймер успешно запущен. Ссылка перестанет работать %s".formatted(
                            TemporalFormatter.formatToDateTime(disableTime));
                    timerInfo = Map.of("isActive", true, "message", message);
                }
            }
        }
        return ResponseEntity.ok().body(
                Map.ofEntries(
                        Map.entry("timerInfo", orNull(timerInfo)),
                        Map.entry(SESSION_QR, orNull(model.getAttribute(SESSION_QR))),
                        Map.entry(CLASS_SESSION, orNull(model.getAttribute(CLASS_SESSION))),
                        Map.entry(CLASS_SESSION_DATE, orNull(model.getAttribute(CLASS_SESSION_DATE))),
                        Map.entry(REVIEW_URL, orNull(model.getAttribute(REVIEW_URL)))
                )
        );
    }

    @PostMapping("start-timer")
    @Parameters(value = {
            @Parameter(name = "sessionId", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "time", in = ParameterIn.QUERY, required = true)
    })
    public ResponseEntity<Map<String, Object>> startTimer(HttpServletRequest request) {
        UUID sessionId = UUID.fromString(request.getParameter(CLASS_SESSION_ID));
        Optional<ClassSession> sessionOpt = sessionService.findSessionById(sessionId);
        if (sessionOpt.isEmpty()) {
            return ResponseEntity.ok().body(
                    Map.of(RESULT, fromActionResult(ActionResultFactory.sessionNotExist())));
        }

        ClassSession session = sessionOpt.get();

        LocalTime time = LocalTime.parse(request.getParameter("time"));
        sessionService.setDisableTimestamp(session, time);

        return ResponseEntity.ok().body(
                Map.of(RESULT, new ActionResultResponse(true,
                        "Таймер успешно запущен. Ссылка перестанет работать %s".formatted(TemporalFormatter.formatToDateTime(time)))));
    }

    @GetMapping("reviews-list")
    @Parameters(value = {
            @Parameter(name = "sessionId", in = ParameterIn.QUERY, required = true)
    })
    public ResponseEntity<Map<String, Object>> getSessionStudents(HttpServletRequest request) {
        ExtendedModelMap model = new ExtendedModelMap();
        sessionController.sessionStudentsPage(request.getParameter(CLASS_SESSION_ID), model);

        ActionResultResponse result = fromActionResult(model.getAttribute(RESULT));
        return ResponseEntity.ok().body(
                Map.of(RESULT, result,
                        REVIEWS_LIST, orNull(model.getAttribute(REVIEWS_LIST))
                )
        );
    }

    @PostMapping("change-professor")
    @Parameters(value = {
            @Parameter(name = "sessionId", in = ParameterIn.QUERY, required = true),
            @Parameter(name = "newProfessor", in = ParameterIn.QUERY, required = true)
    })
    public ResponseEntity<Map<String, Object>> changeProfessor(HttpServletRequest request) {
        ExtendedModelMap model = new ExtendedModelMap();
        sessionController.updateSession(request, model);

        ActionResultResponse result = fromActionResult(model.getAttribute(RESULT));
        return ResponseEntity.ok().body(Map.of(RESULT, result));
    }

}
