package ru.urfu.sv.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.sv.model.domain.entity.Professor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, UUID> {
    Optional<Professor> findByFullNameIgnoreCase(String professorName);
    Optional<Professor> findByUsername(String username);

    List<Professor> findAllByOrderByFullNameAsc();
}
