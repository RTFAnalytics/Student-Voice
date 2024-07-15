package ru.urfu.sv.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.sv.model.domain.Authority;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, String> {
}
