package ru.urfu.sv.model.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Value;
import org.springframework.security.core.userdetails.UserDetails;
import ru.urfu.sv.utils.consts.Roles;

import java.util.Map;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor
public class UserInfo {
    @Id
    @Column(name = "username")
    String username;
    @Column(name = "password")
    String password;
    @Transient
    String role;
    @Transient
    Map<String, String> additionalData;

    @Builder
    public UserInfo(String username, String password, String role, Map<String, String> additionalData) {
        this.username = username;
        this.role = role;
        this.password = password;
        this.additionalData = additionalData;
    }

    public static UserInfo fromUserDetails(UserDetails userDetails) {
        String username = userDetails.getUsername();
        String role = Roles.getRoleFromAuthorities(userDetails.getAuthorities());

        return UserInfo.builder().username(username).role(role).build();
    }
}
