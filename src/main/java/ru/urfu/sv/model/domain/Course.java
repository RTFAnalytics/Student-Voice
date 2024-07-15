package ru.urfu.sv.model.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "courses")
@NoArgsConstructor
@Getter
@ToString
public class Course {
    @Id
    @Column(name = "id", nullable = false)
    private UUID courseId;

    @Embedded
    private CourseDetails courseDetails;

    @Column(name = "create_timestamp", nullable = false)
    private Instant createTimestamp;

    @Transient
    @Getter
    @Setter
    private float avgRating;

    @Builder
    public Course(UUID courseId, CourseDetails courseDetails, Instant createTimestamp) {
        this.courseId = courseId;
        this.courseDetails = courseDetails;
        this.createTimestamp = createTimestamp;
    }
}
