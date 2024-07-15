package ru.urfu.sv.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "class_sessions")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ClassSession {
    @Id
    @Column(name = "id", nullable = false)
    private UUID sessionId;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "course_id", nullable = false)
    private UUID courseId;

    @Embedded
    private CourseDetails courseDetails;

    @Column(name = "room_name")
    private String roomName;

    @Column(name = "session_name", nullable = false)
    private String sessionName;

    @Column(name = "professor_name", nullable = false)
    private String professorName;

    @Column(name = "start_date_time", nullable = false)
    private LocalDateTime startDateTime;

    @Column(name = "end_date_time", nullable = false)
    private LocalDateTime endDateTime;

    @Column(name = "disable_timestamp")
    private Instant disableAfterTimestamp;

    @Column(name = "create_timestamp", nullable = false)
    private Instant createTimestamp;

    @Transient
    @Getter
    @Setter
    private float avgRating;

    @Builder
    public ClassSession(UUID sessionId, String status, UUID courseId, CourseDetails courseDetails, String roomName, String sessionName,
                        String professorName, LocalDateTime startDateTime, LocalDateTime endDateTime, Instant disableAfterTimestamp, Instant createTimestamp) {
        this.sessionId = sessionId;
        this.status = status;
        this.courseId = courseId;
        this.roomName = roomName;
        this.courseDetails = courseDetails;
        this.sessionName = sessionName;
        this.professorName = professorName;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.disableAfterTimestamp = disableAfterTimestamp;
        this.createTimestamp = createTimestamp;
    }
}
