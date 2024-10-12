package ru.urfu.sv.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.sv.model.repository.UserRepository;
import ru.urfu.sv.utils.consts.Parameters;
import ru.urfu.sv.utils.consts.Roles;
import ru.urfu.sv.model.domain.entity.UserInfo;
import ru.urfu.sv.model.repository.AuthorityRepository;
import ru.urfu.sv.utils.result.ActionResult;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final JdbcUserDetailsManager userDetailsManager;
    private final PasswordEncoder encoder;
    private final AuthorityRepository authorityRepository;
    private final UserRepository userRepository;
    private final ProfessorService professorService;

    public boolean isUserExists(String username) {
        return userDetailsManager.userExists(username);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ActionResult createUser(UserInfo userInfo) {
        String username = userInfo.getUsername();
        if (isUserExists(username)) {
            return ActionResultFactory.userExist(username);
        }

        userDetailsManager.createUser(
                User.builder()
                        .passwordEncoder(encoder::encode)
                        .username(username)
                        .password(userInfo.getPassword())
                        .roles(userInfo.getRole())
                        .build());


        return isUserExists(username) ? ActionResultFactory.userCreated() : ActionResultFactory.userCreatingError();
    }

    @Transactional
    public ActionResult updateUser(String username, UserInfo newUserInfo) {
        if (!isUserExists(username)) {
            return ActionResultFactory.userNotExist(username);
        }

        UserDetails current = userDetailsManager.loadUserByUsername(username);

        userDetailsManager.updateUser(User
                .builder()
                .username(username)
                .authorities(current.getAuthorities())
                .passwordEncoder(encoder::encode)
                .password(newUserInfo.getPassword().isBlank() ? current.getPassword() : newUserInfo.getPassword())
                .build());

        if (newUserInfo.getAdditionalData() != null && newUserInfo.getAdditionalData().get(Parameters.PROFESSOR_NAME) != null) {
            ActionResult professorResult = professorService
                    .updateProfessor(username, newUserInfo.getAdditionalData().get(Parameters.PROFESSOR_NAME));
            if (!professorResult.isSuccess()) return professorResult;
        }

        return new ActionResult(true, "Пользователь успешно обновлен");
    }

    public List<UserInfo> findAllUsers() {
        List<String> usernames = userRepository.findAllUsernames();
        return usernames.stream().map(username -> UserInfo.fromUserDetails(userDetailsManager.loadUserByUsername(username))).toList();
    }

    public Optional<UserInfo> findUserByUsername(String username) {
        if (!isUserExists(username)) return Optional.empty();

        return Optional.of(UserInfo.fromUserDetails(userDetailsManager.loadUserByUsername(username)));
    }

    @Transactional
    public ActionResult deleteUser(String username) {
        userDetailsManager.deleteUser(username);
        return new ActionResult(true, "Пользователь %s успешно удален", username);
    }

    public boolean isAnyAdminExists() {
        String adminAuth = "ROLE_".concat(Roles.ADMIN);
        return authorityRepository
                .findAll()
                .stream()
                .anyMatch(auth -> adminAuth.equals(auth.getAuthority()));
    }
}
