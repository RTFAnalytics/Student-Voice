package ru.urfu.sv.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.urfu.sv.model.domain.entity.ClassSession;
import ru.urfu.sv.model.domain.entity.Course;
import ru.urfu.sv.model.domain.entity.Review;
import ru.urfu.sv.services.ClassSessionService;
import ru.urfu.sv.services.CourseService;
import ru.urfu.sv.services.ReviewService;
import ru.urfu.sv.services.ReviewsReportService;
import ru.urfu.sv.utils.consts.Templates;
import ru.urfu.sv.utils.formatters.TemporalFormatter;
import ru.urfu.sv.utils.result.ActionResult;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.io.PrintWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static ru.urfu.sv.utils.consts.Parameters.*;

@Controller
@Slf4j
@RequestMapping("/reviews")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;
    @Autowired
    private ClassSessionService sessionService;
    @Autowired
    private CourseService courseService;
    @Autowired
    private ReviewsReportService reportService;

    @GetMapping("/create")
    public String createReviewPage(@RequestParam("sessionId") String sessionIdStr, Model model) {
        UUID sessionId = UUID.fromString(sessionIdStr);
        Optional<ClassSession> sessionOpt = sessionService.findSessionById(sessionId);
        if (sessionOpt.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.sessionNotExist());
            return Templates.REVIEW_STATUS;
        }

        if (sessionOpt.get().getDisableAfterTimestamp() != null && Instant.now().isAfter(sessionOpt.get().getDisableAfterTimestamp())) {
            model.addAttribute(RESULT, new ActionResult(false, "Отзыв на эту пару больше нельзя оставить"));
            return Templates.REVIEW_STATUS;
        }

        Optional<Course> courseOpt = courseService.findCourseBySession(sessionOpt.get());
        if (courseOpt.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.courseNotExist());
            return Templates.REVIEW_STATUS;
        }

        Course course = courseOpt.get();
        ClassSession session = sessionOpt.get();
        model.addAttribute(COURSE_NAME, course.getCourseDetails().getCourseName());
        model.addAttribute("address", "%s / %s".formatted(course.getCourseDetails().getInstituteAddress(), session.getRoomName()));
        model.addAttribute(CLASS_SESSION_DATE, TemporalFormatter.formatToDateTime(session.getStartDateTime()));
        model.addAttribute(PROFESSOR_NAME, session.getProfessorName());

        return Templates.CREATE_REVIEW;
    }

    @GetMapping("/download-report")
    @ResponseBody
    public ResponseEntity<StreamingResponseBody> downloadReport(){
        String content = reportService.getCvsReport();
        String fileName = "reviews_report".concat("_")
                .concat(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm")))
                .concat(".csv");

        StreamingResponseBody responseBody = outputStream -> {
            try (PrintWriter writer = new PrintWriter(outputStream)) {
                writer.write(content);
            }
        };

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        headers.add(HttpHeaders.CONTENT_TYPE, "text/plain; charset=UTF-8");

        return new ResponseEntity<>(responseBody, headers, HttpStatus.OK);
    }

    @RequestMapping(path = "/download-report-xslx", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    @SneakyThrows
    public ResponseEntity<byte[]> downloadReportXSLX(HttpServletResponse response) {

        final String fileName = "reviews_report".concat("_")
                .concat(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm")))
                .concat(".xlsx");

        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName);
        response.setContentType(HttpHeaders.CONTENT_TYPE);
        final byte[] report = reviewService.getReport();

        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @PostMapping("/save")
    public String saveReview(HttpServletRequest request, Model model) {
        Optional<ClassSession> sessionOpt = sessionService.findSessionById(UUID.fromString(request.getParameter(CLASS_SESSION_ID)));
        if(sessionOpt.isEmpty()){
            model.addAttribute(RESULT, ActionResultFactory.sessionNotExist());
            return Templates.REVIEW_STATUS;
        }
        if (sessionOpt.get().getDisableAfterTimestamp() != null && Instant.now().isAfter(sessionOpt.get().getDisableAfterTimestamp())) {
            model.addAttribute(RESULT, new ActionResult(false, "Отзыв на эту пару больше нельзя оставить"));
            return Templates.REVIEW_STATUS;
        }

        ActionResult result = saveReview(request.getParameterMap());
        model.addAttribute(RESULT, result);

        if (result.isSuccess()) {
            return Templates.REVIEW_STATUS;
        } else {
            return Templates.CREATE_REVIEW;
        }
    }

    private ActionResult saveReview(Map<String, String[]> parameters) {
        Review review = Review
                .builder()
                .sessionId(UUID.fromString(parameters.get(CLASS_SESSION_ID)[0]))
                .studentFullName(parameters.get(STUDENT_FULL_NAME)[0])
                .value(Byte.valueOf(parameters.get(REVIEW_VALUE)[0]))
                .comment(parameters.get(COMMENT)[0])
                .timestamp(Instant.now())
                .build();

        try {
            ActionResult result = reviewService.saveReview(review);
            log.info(result.getFormattedMessage());
            return result;
        } catch (IllegalArgumentException e) {
            log.error("Отзыв не был сохранен", e);
            return ActionResultFactory.reviewCreatingError();
        }
    }
}
