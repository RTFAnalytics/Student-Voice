package ru.urfu.sv.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.sv.model.domain.Professor;
import ru.urfu.sv.model.repository.ProfessorRepository;
import ru.urfu.sv.utils.result.ActionResult;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfessorService {
    private final ProfessorRepository repository;
    private final CourseService courseService;
    private final ClassSessionService sessionService;

    public List<Professor> getAllProfessors() {
        return repository.findAllByOrderByFullNameAsc();
    }

    @Transactional
    public ActionResult createProfessor(String username, String professorName) {
        Optional<Professor> professorOptByUsername = repository.findByUsername(username);
        if (professorOptByUsername.isPresent()) {
            Professor professor = professorOptByUsername.get();
            log.warn("Профессор с username={} уже существует - {}", username, professor);
            return ActionResultFactory.userExist(username);
        }

        Optional<Professor> professorOptByFullName = repository.findByFullNameIgnoreCase(professorName);
        if (professorOptByFullName.isPresent()) {
            Professor professor = professorOptByFullName.get();
            log.warn("Профессор с именем {} уже существует - {}", professorName, professor);
            return ActionResultFactory.professorExist();
        }

        Professor professor = repository.save(new Professor(username, professorName));
        return repository.existsById(professor.getProfessorId()) ? ActionResultFactory.professorCreated() : ActionResultFactory.professorCreatingError();
    }

    @Transactional
    public ActionResult updateProfessor(String username, String newProfessorName) {
        Optional<Professor> professor = findProfessorByUsername(username);
        if (professor.isEmpty()) {
            return ActionResultFactory.professorNotExist(username);
        }
        String oldProfessorName = professor.get().getFullName();
        professor.get().setFullName(newProfessorName);
        repository.save(professor.get());

        courseService.updateProfessor(oldProfessorName, newProfessorName);
        sessionService.updateProfessorName(oldProfessorName, newProfessorName);

        return ActionResultFactory.professorCreated();
    }

    public Optional<Professor> findProfessorByUsername(String username) {
        return repository.findByUsername(username);
    }

}
