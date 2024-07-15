package ru.urfu.sv.model.repository;

import com.opencsv.bean.CsvBindByPosition;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.ConstructorResult;
import jakarta.persistence.NamedNativeQuery;
import jakarta.persistence.SqlResultSetMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.urfu.sv.model.domain.Review;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findAllBySessionId(UUID sessionId);

    @Query(nativeQuery = true, value = "select * from reviews where session_id in (:ids)")
    List<Review> findAllBySessionsIds(@Param("ids") List<UUID> sessionsIds);
}
