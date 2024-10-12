package ru.urfu.sv.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.sv.model.domain.entity.ClassSession;
import ru.urfu.sv.model.domain.entity.Course;
import ru.urfu.sv.model.domain.dto.CourseDetails;
import ru.urfu.sv.model.domain.entity.Institute;
import ru.urfu.sv.model.repository.CourseRepository;
import ru.urfu.sv.utils.result.ActionResult;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {
    private final CourseRepository repository;
    private final InstituteService instituteService;

    @Transactional
    public ActionResult createCourse(UUID courseId, CourseDetails courseDetails) {
        if (repository.existsById(courseId)){
            log.warn("Курс с идентификатором {} уже существует", courseId);
            return ActionResultFactory.courseExist();
        }

        Optional<Institute> institute = instituteService.findInstituteByAddress(courseDetails.getInstituteAddress());
        institute.ifPresent(value -> courseDetails.setInstituteName(value.getShortName()));

        Course course = repository.save(new Course(courseId, courseDetails, Instant.now()));
        return repository.existsById(course.getCourseId()) ? ActionResultFactory.courseCreated() : ActionResultFactory.courseCreatingError();
    }

    @Transactional
    public void createCoursesByClassSessions(List<ClassSession> sessions) {
        for (ClassSession session : sessions) {
            if (!repository.existsById(session.getCourseId())) {
                ActionResult result = createCourse(session.getCourseId(), session.getCourseDetails());
                if (!result.isSuccess()) {
                    log.error("Предмет {} не создался - {}", session.getCourseDetails().getCourseName(), result.getFormattedMessage());
                } else {
                    log.info("Создался новый предмет - {}", repository.findById(session.getCourseId()));
                }
            }
        }
    }

    @Transactional
    public ActionResult updateProfessor(String professorName, String newProfessorName){
        List<Course> courses = findCoursesByProfessorName(professorName);
        courses.forEach(course -> {
            String names = course.getCourseDetails().getProfessorsNames();
            if(names.contains(professorName)){
                course.getCourseDetails().setProfessorsNames(names.replace(professorName, newProfessorName));
                repository.save(course);
            }
        });

        return new ActionResult(true, "Предметы успешно обновлены");
    }

    public Optional<Course> findCourseByName(String courseName) {
        return repository.findByCourseDetails_CourseName(courseName);
    }

    public Optional<Course> findCourseById(UUID courseId) {
        return repository.findById(courseId);
    }

    public Optional<Course> findCourseBySession(ClassSession classSession) {
        return repository.findById(classSession.getCourseId());
    }

    public List<Course> findCoursesByProfessorName(String professorName) {
        List<Course> courses = repository.findAllByCourseDetails_ProfessorsNamesContainsIgnoreCase(professorName);
        String coursesList = String.join("\n", courses.stream().map(Course::toString).toList());
        log.info("Для профессора {} найдены следующие предметы: {}", professorName, coursesList);
        return courses;
    }

    public List<Course> findCoursesByInstituteName(String instituteShortName) {
        return repository.findAllByCourseDetails_InstituteName(instituteShortName);
    }

    public List<Course> findAll() {
        return repository.findAll();
    }
}
