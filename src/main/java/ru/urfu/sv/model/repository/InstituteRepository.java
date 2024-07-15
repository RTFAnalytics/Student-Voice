package ru.urfu.sv.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.urfu.sv.model.domain.Institute;

import java.util.Optional;

@Repository
public interface InstituteRepository extends JpaRepository<Institute, Integer> {
    Optional<Institute> findByFullNameIgnoreCase(String fullName);

    Optional<Institute> findByShortNameIgnoreCase(String shortName);

    boolean existsByAddress(String address);
    Optional<Institute> findByAddressIgnoreCase(String address);
}
