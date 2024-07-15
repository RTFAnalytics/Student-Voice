package ru.urfu.sv.model.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CourseDetails {
    @Column(name = "course_name", nullable = false)
    private String courseName;

    @Column(name = "institute_name")
    private String instituteName;

    @Column(name = "institute_address", nullable = false)
    private String instituteAddress;

    @Column(name = "professors", nullable = false)
    private String professorsNames;
}
