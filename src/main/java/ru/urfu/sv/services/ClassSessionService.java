package ru.urfu.sv.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.sv.model.domain.ClassSession;
import ru.urfu.sv.model.domain.Course;
import ru.urfu.sv.model.repository.ClassSessionRepository;
import ru.urfu.sv.utils.exceptions.ModeusException;
import ru.urfu.sv.utils.result.ActionResult;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.time.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClassSessionService {
    private final ClassSessionRepository repository;
    private final CourseService courseService;
    private final InstituteService instituteService;
    private final ModeusService modeusService;

    @Transactional
    public ActionResult createClassSession(UUID courseId, String sessionName, String roomName, String professorName,
                                           LocalDateTime startDateTime, LocalDateTime endDateTime) {
        Optional<Course> courseOpt = courseService.findCourseById(courseId);

        if (courseOpt.isEmpty()) {
            log.error("Предмета с идентификатором {} не найдено", courseId);
            return ActionResultFactory.courseNotExist();
        }

        ClassSession session = ClassSession
                .builder()
                .sessionId(UUID.randomUUID())
                .status("Запланировано")
                .sessionName(sessionName)
                .roomName(roomName)
                .courseId(courseId)
                .courseDetails(courseOpt.get().getCourseDetails())
                .professorName(professorName)
                .startDateTime(startDateTime)
                .endDateTime(endDateTime)
                .disableAfterTimestamp(endDateTime.toInstant(ZoneOffset.ofHours(5)))
                .createTimestamp(Instant.now())
                .build();

        session = repository.save(session);

        return repository.existsById(session.getSessionId()) ? ActionResultFactory.sessionCreated() : ActionResultFactory.sessionCreatingError();
    }

    @Transactional
    public ActionResult setDisableTimestamp(ClassSession session, LocalTime duration) {
        Instant disableTimestamp = Instant.now()
                .plus(Duration.ofHours(duration.getHour())
                        .plus(Duration.ofMinutes(duration.getMinute()))
                        .plus(Duration.ofSeconds(duration.getSecond())));
        session.setDisableAfterTimestamp(disableTimestamp);
        repository.save(session);

        return repository.existsById(session.getSessionId()) ? ActionResultFactory.sessionCreated() : ActionResultFactory.sessionCreatingError();
    }

    @Transactional
    public ActionResult updateProfessorName(String professorName, String newProfessorName) {
        List<ClassSession> sessions = findAllSavedClassSessionsByProfessorName(professorName);
        sessions.forEach(session -> {
            String names = session.getCourseDetails().getProfessorsNames();
            String name = session.getProfessorName();
            if (names.contains(professorName)) {
                session.getCourseDetails().setProfessorsNames(names.replace(professorName, newProfessorName));
                repository.save(session);
            }
            if (name.contains(professorName)) {
                session.setProfessorName(newProfessorName);
                repository.save(session);
            }
        });

        return new ActionResult(true, "Пары успешно обновлены");
    }

    @Transactional
    public ActionResult updateSessionProfessor(UUID sessionId, String professorName) {
        Optional<ClassSession> session = repository.findById(sessionId);
        session.orElseThrow().setProfessorName(professorName);
        repository.save(session.orElseThrow());
        return new ActionResult(true, "Пара успешно изменена");
    }

    @Transactional
    public List<ClassSession> findAllClassSessionsByProfessorName(String professorName, LocalDate dateFrom, LocalDate dateTo) throws ModeusException {
        final List<UUID> savedClassSessions = repository.findAllByProfessorNameIgnoreCase(professorName).stream().map(ClassSession::getSessionId).toList();
        List<ClassSession> modeusClassSessions = modeusService.getSessionsOfProfessor(professorName, dateFrom, dateTo);
        List<ClassSession> unsaved = modeusClassSessions.stream().filter(session -> !savedClassSessions.contains(session.getSessionId())).toList();
        if (!unsaved.isEmpty()) {
            String newClassSessionStr = String.join("\n", unsaved.stream().map(ClassSession::toString).toList());
            log.info("Для преподавателя найдены новые пары {}", newClassSessionStr);
            instituteService.createInstitutesByClassSessions(unsaved);
            courseService.createCoursesByClassSessions(unsaved);
            saveAll(unsaved);
        }

        List<ClassSession> allClassSessions = repository.findAllByProfessorNameIgnoreCase(professorName);
        String classSessionsStr = String.join("\n", allClassSessions.stream().map(ClassSession::toString).toList());
        log.info("Для преподавателя {} найдены следующие пары: {}", professorName, classSessionsStr);
        return allClassSessions;
    }

    private ActionResult saveAll(List<ClassSession> sessions) {
        sessions.forEach(session -> session.setDisableAfterTimestamp(session.getEndDateTime().toInstant(ZoneOffset.ofHours(5))));
        repository.saveAll(sessions);
        return ActionResultFactory.sessionCreated();
    }

    public List<ClassSession> findSavedClassSessionsByProfessorName(String professorName, LocalDate dateFrom, LocalDate dateTo) {
        final List<ClassSession> savedClassSessions = repository
                .findAllByProfessorNameIgnoreCaseAndStartDateTimeAfterAndEndDateTimeBefore(professorName, dateFrom.atStartOfDay(), dateTo.atStartOfDay());
        String classSessionsStr = String.join("\n", savedClassSessions.stream().map(ClassSession::toString).toList());
        log.info("Для преподавателя {} найдены следующие пары: {}", professorName, classSessionsStr);
        return savedClassSessions;
    }

    public List<ClassSession> findAllSavedClassSessionsByProfessorName(String professorName) {
        final List<ClassSession> savedClassSessions = repository
                .findAllByProfessorNameIgnoreCase(professorName);
        String classSessionsStr = String.join("\n", savedClassSessions.stream().map(ClassSession::toString).toList());
        log.info("Для преподавателя {} найдены следующие пары: {}", professorName, classSessionsStr);
        return savedClassSessions;
    }

    public List<ClassSession> findClassSessionsByCourseId(UUID courseId) {
        return repository.findAllByCourseId(courseId);
    }

    public List<ClassSession> findClassSessionsByInstituteName(String instituteShortName) {
        return repository.findAllByCourseDetails_InstituteNameIgnoreCase(instituteShortName);
    }

    public List<ClassSession> getClassSessionsAfterDate(LocalDateTime dateTime, List<ClassSession> classSessions) {
        return classSessions.stream().filter(session -> {
            LocalDateTime sessionDate = session.getStartDateTime();
            return sessionDate.isAfter(dateTime);
        }).toList();
    }

    public Optional<ClassSession> findSessionById(UUID sessionId) {
        return repository.findById(sessionId);
    }
}
