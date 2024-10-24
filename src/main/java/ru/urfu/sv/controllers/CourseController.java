package ru.urfu.sv.controllers;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.urfu.sv.model.domain.entity.ClassSession;
import ru.urfu.sv.model.domain.entity.Course;
import ru.urfu.sv.model.domain.dto.CourseDetails;
import ru.urfu.sv.model.domain.entity.Institute;
import ru.urfu.sv.services.ClassSessionService;
import ru.urfu.sv.services.CourseService;
import ru.urfu.sv.services.InstituteService;
import ru.urfu.sv.services.ProfessorService;
import ru.urfu.sv.utils.consts.Templates;
import ru.urfu.sv.utils.result.ActionResult;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.urfu.sv.utils.consts.Parameters.*;
import static ru.urfu.sv.utils.model.ModelUtils.addProfessorName;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/courses")
@PreAuthorize("@AuthoritiesAC.isAdminOrProfessor()")
public class CourseController {
    private final CourseService courseService;
    private final ProfessorService professorService;
    private final ClassSessionService sessionService;
    private final InstituteService instituteService;

    @GetMapping("/create")
    public String createCoursePage(
            @RequestParam(name = INSTITUTE_ID, required = false, defaultValue = "-1") Integer instituteId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        if (instituteId != -1) {
            Optional<Institute> instituteOpt = instituteService.findInstituteById(instituteId);
            if (instituteOpt.isEmpty()) {
                model.addAttribute(RESULT, ActionResultFactory.instituteNotExist());
                return Templates.CREATE_COURSE;
            }
            model.addAttribute(INSTITUTE_ID, instituteId);
        }
        model.addAttribute(INSTITUTES_LIST, instituteService.findAllInstitutes());
        addProfessorName(professorService, model, userDetails);
        return Templates.CREATE_COURSE;
    }

    @PostMapping("/create")
    public String createCourse(@AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request, Model model) {
        Integer instituteId = Integer.parseInt(request.getParameter(INSTITUTE_ID));
        String courseName = request.getParameter(COURSE_NAME);
        String professorsNames = request.getParameter(PROFESSORS_NAMES);
        Optional<Institute> institute = instituteService.findInstituteById(instituteId);

        ActionResult result;
        if (institute.isEmpty()) {
            result = ActionResultFactory.instituteNotExist();
        } else {
            CourseDetails courseDetails = CourseDetails.builder()
                    .instituteName(institute.get().getShortName())
                    .instituteAddress(institute.get().getAddress())
                    .courseName(courseName)
                    .professorsNames(professorsNames)
                    .build();

            result = courseService.createCourse(UUID.randomUUID(), courseDetails);
            model.addAttribute(INSTITUTES_LIST, instituteService.findAllInstitutes());
            addProfessorName(professorService, model, userDetails);
        }

        model.addAttribute(RESULT, result);
        return Templates.CREATE_COURSE;
    }

    @GetMapping("/{courseId}")
    public String coursePage(@PathVariable("courseId") UUID courseId, Model model) {
        Optional<Course> courseOpt = courseService.findCourseById(courseId);
        if (courseOpt.isEmpty()) {
            model.addAttribute(RESULT, ActionResultFactory.courseNotExist());
            return Templates.COURSE_PAGE;
        }

        List<ClassSession> sessions = sessionService.findClassSessionsByCourseId(courseId);
        model.addAttribute(CLASS_SESSIONS_LIST, sessions);
        model.addAttribute(COURSE_DETAILS, courseOpt.get().getCourseDetails());
        model.addAttribute(COURSE_ID, courseId);
        return Templates.COURSE_PAGE;
    }
}
