package ru.urfu.sv.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.urfu.sv.model.domain.dto.CourseDetails;
import ru.urfu.sv.model.domain.entity.*;
import ru.urfu.sv.services.ClassSessionService;
import ru.urfu.sv.services.CourseService;
import ru.urfu.sv.services.InstituteService;
import ru.urfu.sv.services.ReviewService;
import ru.urfu.sv.utils.consts.Templates;
import ru.urfu.sv.utils.formatters.TemporalFormatter;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.urfu.sv.utils.consts.Parameters.*;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/rating")
@PreAuthorize("@AuthoritiesAC.isAdmin()")
public class RatingController {
    private final InstituteService instituteService;
    private final CourseService courseService;
    private final ClassSessionService sessionService;
    private final ReviewService reviewService;

    @GetMapping("/institutes")
    public String institutesRating(Model model) {
        List<Institute> instituteList = instituteService.findAllInstitutes();

        instituteList.forEach(inst -> {
            List<ClassSession> sessions = sessionService.findClassSessionsByInstituteName(inst.getShortName());
            inst.setAvgRating(reviewService.getAverageRatingBySessions(sessions));
        });

        model.addAttribute(INSTITUTES_LIST, instituteList);

        return Templates.RATING_INSTITUTES;
    }

    @GetMapping("/institute/{instituteId}")
    public String instituteRating(@PathVariable(INSTITUTE_ID) Integer instituteId,
                                  Model model) {
        Optional<Institute> instituteOpt = instituteService.findInstituteById(instituteId);
        if (instituteOpt.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.instituteNotExist());
            return Templates.RATING_INSTITUTE;
        }

        List<Course> courses = courseService.findCoursesByInstituteName(instituteOpt.get().getShortName());
        courses.forEach(course -> {
            List<ClassSession> sessions = sessionService.findClassSessionsByCourseId(course.getCourseId());
            course.setAvgRating(reviewService.getAverageRatingBySessions(sessions));
        });
        model.addAttribute("institute", instituteOpt.get());
        model.addAttribute(COURSES_LIST, courses);
        return Templates.RATING_INSTITUTE;
    }

    @GetMapping("/course/{courseId}")
    public String courseRating(@PathVariable(COURSE_ID) UUID courseId,
                               Model model) {
        Optional<Course> courseOpt = courseService.findCourseById(courseId);
        if (courseOpt.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.courseNotExist());
            return Templates.RATING_COURSE;
        }

        List<ClassSession> sessions = sessionService.findClassSessionsByCourseId(courseId);
        sessions.forEach(session -> session.setAvgRating(reviewService.getAverageRatingBySessions(Collections.singletonList(session))));

        model.addAttribute(COURSE_NAME, courseOpt.get().getCourseDetails().getCourseName());
        model.addAttribute(CLASS_SESSIONS_LIST, sessions);
        return Templates.RATING_COURSE;
    }

    @GetMapping("/session/{sessionId}")
    public String sessionRating(@PathVariable(CLASS_SESSION_ID) String sessionIdStr,
                                Model model) {
        UUID sessionId = UUID.fromString(sessionIdStr);
        Optional<ClassSession> sessionOpt = sessionService.findSessionById(sessionId);
        if (sessionOpt.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.sessionNotExist());
            return Templates.RATING_COURSE;
        }

        List<Review> reviews = reviewService.findReviewsBySessionId(sessionId);

        model.addAttribute(REVIEWS_LIST, reviews);
        String sessionDate = TemporalFormatter.formatToDateTime(sessionOpt.get().getStartDateTime());
        CourseDetails details = sessionOpt.get().getCourseDetails();
        model.addAttribute(CLASS_SESSION, "%s, %s / %s - %s"
                .formatted(details.getCourseName(), details.getInstituteAddress(), sessionOpt.get().getRoomName(), sessionDate));
        return Templates.RATING_SESSION;
    }
}
