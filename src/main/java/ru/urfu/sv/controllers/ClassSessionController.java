package ru.urfu.sv.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.urfu.sv.model.domain.ClassSession;
import ru.urfu.sv.model.domain.Course;
import ru.urfu.sv.model.domain.Professor;
import ru.urfu.sv.model.domain.Review;
import ru.urfu.sv.services.*;
import ru.urfu.sv.utils.formatters.TemporalFormatter;
import ru.urfu.sv.utils.result.ActionResult;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.urfu.sv.utils.consts.Parameters.*;
import static ru.urfu.sv.utils.consts.Templates.*;
import static ru.urfu.sv.utils.model.ModelUtils.addProfessorName;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/sessions")
@PreAuthorize("hasAnyRole('ADMIN', 'PROFESSOR')")
public class ClassSessionController {
    private final ClassSessionService sessionService;
    private final ReviewService reviewService;
    private final CourseService courseService;
    private final ProfessorService professorService;
    private final InstituteService instituteService;
    private final QRCodeService qrCodeService;

    @Value("${application.host}")
    private String applicationHost;

    @GetMapping("/create")
    public String createSessionPage(@RequestParam(value = COURSE_ID, required = false) UUID courseId,
                                    @AuthenticationPrincipal UserDetails userDetails,
                                    Model model) {
        model.addAttribute(INSTITUTES_LIST, instituteService.findAllInstitutes());
        model.addAttribute(COURSES_LIST, courseService.findAll());
        addProfessorName(professorService, model, userDetails);

        if (courseId != null) {
            Optional<Course> courseOpt = courseService.findCourseById(courseId);
            if (courseOpt.isEmpty()) {
                model.addAttribute(RESULT, ActionResultFactory.courseNotExist());
                return CREATE_SESSION;
            }
            model.addAttribute("requestedCourse", courseOpt.get());
        }

        return CREATE_SESSION;
    }

    @PostMapping("/create")
    public String createSession(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request, Model model) {
        UUID courseId = UUID.fromString(request.getParameter(COURSE_ID));
        String professorName = request.getParameter(PROFESSOR_NAME);
        LocalDateTime startSessionDate = LocalDateTime.parse(request.getParameter("startSession"));
        LocalDateTime endSessionDate = LocalDateTime.parse(request.getParameter("endSession"));
        String roomName = request.getParameter("roomName");
        String sessionName = request.getParameter("sessionName");

        ActionResult result = sessionService.createClassSession(courseId, sessionName, roomName, professorName,
                startSessionDate, endSessionDate);

        model.addAttribute(INSTITUTES_LIST, instituteService.findAllInstitutes());
        model.addAttribute(COURSES_LIST, courseService.findAll());
        model.addAttribute(RESULT, result);
        addProfessorName(professorService, model, userDetails);
        return CREATE_SESSION;
    }

    @GetMapping("/{sessionId}")
    public String sessionPage(@PathVariable("sessionId") String sessionIdStr, Model model) {
        UUID sessionId = UUID.fromString(sessionIdStr);
        Optional<ClassSession> sessionOpt = sessionService.findSessionById(sessionId);

        if (sessionOpt.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.sessionNotExist());
            return SESSION_PAGE;
        }

        ClassSession session = sessionOpt.get();

        if (session.getDisableAfterTimestamp() != null) {
            if (Instant.now().isAfter(session.getDisableAfterTimestamp())) {
                model.addAttribute(RESULT, new ActionResult(false, "Время действия кода для отзыва вышло"));
            } else {
                //Костыль для зоны UTC+5
                OffsetDateTime disableTime = session.getDisableAfterTimestamp().atOffset(ZoneOffset.ofHours(5));
                model.addAttribute(RESULT, new ActionResult(true, "Таймер успешно запущен. Ссылка перестанет работать %s",
                        TemporalFormatter.formatToDateTime(disableTime)));
            }
        }

        fillSessionModel(model, session);
        return SESSION_PAGE;
    }

    @PostMapping("/start-timer")
    public String startTimer(HttpServletRequest request, Model model) {
        UUID sessionId = UUID.fromString(request.getParameter("sessionId"));
        Optional<ClassSession> sessionOpt = sessionService.findSessionById(sessionId);
        if (sessionOpt.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.sessionNotExist());
            return SESSION_PAGE;
        }

        ClassSession session = sessionOpt.get();

        LocalTime time = LocalTime.parse(request.getParameter("time"));
        sessionService.setDisableTimestamp(session, time);

        return REDIRECT.concat("/sessions/").concat(sessionId.toString());
    }

    @GetMapping("/{sessionId}/students")
    public String sessionStudentsPage(@PathVariable("sessionId") String sessionIdStr, Model model) {
        Optional<ClassSession> sessionOpt = sessionService.findSessionById(UUID.fromString(sessionIdStr));
        if (sessionOpt.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.sessionNotExist());
            return SESSION_STUDENTS;
        }
        ClassSession session = sessionOpt.get();
        List<Review> reviews = reviewService.findReviewsBySessionId(session.getSessionId());
        model.addAttribute(REVIEWS_LIST, reviews);
        model.addAttribute(CLASS_SESSION, session);
        model.addAttribute(CLASS_SESSION_DATE, TemporalFormatter.formatToSessionDateTime(session));
        return SESSION_STUDENTS;
    }

    @GetMapping("/{sessionId}/update")
    public String updateSessionPage(@PathVariable("sessionId") String sessionIdStr, Model model) {
        Optional<ClassSession> session = sessionService.findSessionById(UUID.fromString(sessionIdStr));
        if (session.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.sessionNotExist());
            return UPDATE_SESSION;
        }
        List<Professor> professors = professorService.getAllProfessors();
        model.addAttribute(PROFESSORS_LIST, professors);
        model.addAttribute(CLASS_SESSION, session.get());
        model.addAttribute(CLASS_SESSION_DATE, TemporalFormatter.formatToSessionDateTime(session.get()));
        return UPDATE_SESSION;
    }

    @PostMapping("update")
    public String updateSession(HttpServletRequest request, Model model) {
        UUID sessionId = UUID.fromString(request.getParameter(CLASS_SESSION_ID));
        Optional<ClassSession> session = sessionService.findSessionById(sessionId);
        if (session.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.sessionNotExist());
            return UPDATE_SESSION;
        }
        String newProfessor = request.getParameter("newProfessor");
        ActionResult result = sessionService.updateSessionProfessor(sessionId, newProfessor);
        model.addAttribute(RESULT, result);
        return REDIRECT.concat("/professor-home");
    }

    private void fillSessionModel(Model model, ClassSession session) {
        String reviewUrl = "%s/reviews/create?sessionId=%s".formatted(applicationHost, session.getSessionId());
        String qrCode = qrCodeService.getEncodedCode(reviewUrl, 256, 256);
        model.addAttribute(SESSION_QR, qrCode);
        model.addAttribute(CLASS_SESSION, session);
        model.addAttribute(CLASS_SESSION_DATE, TemporalFormatter.formatToSessionDateTime(session));
        model.addAttribute(REVIEW_URL, reviewUrl);
    }
}
