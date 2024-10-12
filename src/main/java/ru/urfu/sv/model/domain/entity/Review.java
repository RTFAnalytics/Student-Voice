package ru.urfu.sv.model.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.Immutable;
import ru.urfu.sv.utils.model.ReviewInfo;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "reviews")
@Immutable
@NoArgsConstructor
@Getter
@ToString
@SqlResultSetMapping(name = "reviewInfoMapping",
        classes = {
                @ConstructorResult(
                        targetClass = ReviewInfo.class,
                        columns = {@ColumnResult(name = "review_id", type = UUID.class),
                                @ColumnResult(name = "review_value", type = byte.class),
                                @ColumnResult(name = "student_full_name"),
                                @ColumnResult(name = "review_comment"),
                                @ColumnResult(name = "session_name"),
                                @ColumnResult(name = "course_name"),
                                @ColumnResult(name = "professors"),
                                @ColumnResult(name = "professor_name"),
                                @ColumnResult(name = "institute_name"),
                                @ColumnResult(name = "institute_address"),
                                @ColumnResult(name = "room_name"),
                                @ColumnResult(name = "review_timestamp")})
        })
@NamedNativeQuery(name = "findAllReviewInfo",
        query = """
                SELECT
                r.id as review_id,
                r.review_value,
                r.student_full_name,
                r.review_comment,
                cs.session_name,
                cs.course_name,
                cs.professors,
                cs.professor_name,
                cs.institute_name,
                cs.institute_address,
                cs.room_name,
                r.review_timestamp
                FROM reviews r LEFT JOIN class_sessions cs on r.session_id = cs.id;
                """,
        resultSetMapping = "reviewInfoMapping")
public class Review {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID reviewId;

    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    @Column(name = "student_full_name", nullable = false)
    private String studentFullName;

    @Column(name = "review_value", nullable = false)
    private Byte value;

    @Column(name = "review_comment")
    private String comment;

    @Column(name = "review_timestamp", nullable = false)
    private Instant timestamp;

    @Builder
    public Review(UUID sessionId, String studentFullName, Byte value, String comment, Instant timestamp) {
        this.sessionId = sessionId;
        this.studentFullName = studentFullName;
        this.value = value;
        this.comment = comment;
        this.timestamp = timestamp;
    }
}
