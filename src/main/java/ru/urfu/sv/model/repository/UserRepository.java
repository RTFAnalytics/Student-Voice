package ru.urfu.sv.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.urfu.sv.model.domain.entity.UserInfo;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<UserInfo, String> {
    @Query(value = "select username from users", nativeQuery = true)
    List<String> findAllUsernames();

    UserInfo findByUsername(String username);
}