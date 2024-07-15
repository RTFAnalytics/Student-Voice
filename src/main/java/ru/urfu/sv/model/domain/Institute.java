package ru.urfu.sv.model.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "institutes")
@NoArgsConstructor
@Getter
@ToString
public class Institute {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer instituteId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "short_name", nullable = false)
    private String shortName;

    @Column(name = "address", nullable = false)
    private String address;

    @Transient
    @Getter
    @Setter
    private float avgRating;

    public Institute(String fullName, String shortName, String address) {
        this.fullName = fullName;
        this.shortName = shortName;
        this.address = address;
    }
}
