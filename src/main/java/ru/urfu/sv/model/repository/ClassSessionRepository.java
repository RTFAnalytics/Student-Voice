package ru.urfu.sv.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.sv.model.domain.entity.ClassSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClassSessionRepository extends JpaRepository<ClassSession, UUID> {
    List<ClassSession> findAllByCourseId(UUID courseId);
    List<ClassSession> findAllByProfessorNameIgnoreCase(String professorName);
    List<ClassSession> findAllByProfessorNameIgnoreCaseAndStartDateTimeAfterAndEndDateTimeBefore(String professorName, LocalDateTime from, LocalDateTime to);
    List<ClassSession> findAllByCourseDetails_InstituteNameIgnoreCase(String instituteName);
}
