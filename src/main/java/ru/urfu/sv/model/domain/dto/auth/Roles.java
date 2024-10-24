package ru.urfu.sv.model.domain.dto.auth;

import lombok.Getter;

@Getter
public enum Roles {

    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_PROFESSOR("ROLE_PROFESSOR");

    private final String name;

    Roles(String name) {
        this.name = name;
    }
}