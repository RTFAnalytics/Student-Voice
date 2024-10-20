package ru.urfu.sv.services.jwt;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.urfu.sv.model.domain.entity.UserInfo;
import ru.urfu.sv.model.repository.UserRepository;

import java.util.ArrayList;

import static java.util.Objects.isNull;

/**
 * Сервис для работы с user
 *
 * @author Sazhin Egor
 * @since 20.10.2024
 */
@Service
public class JwtUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        final UserInfo user = userRepository.findByUsername(username);
        if (isNull(user)) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }

        return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
    }
}
