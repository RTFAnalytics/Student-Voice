package ru.urfu.sv.controllers.jwt;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.urfu.sv.controllers.links.Links;
import ru.urfu.sv.model.domain.dto.auth.UserClient;
import ru.urfu.sv.services.jwt.JwtUserDetailsService;
import ru.urfu.sv.utils.jwt.JwtUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Контролер предназначен для авторизации, чтобы сгенерировать токен
 *
 * @author Sazhin Egor
 * @since 20.10.2024
 */
@RestController
@RequestMapping(Links.BASE_API)
public class JwtAuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUserDetailsService userDetailsService;
    @Autowired
    private JwtUtil jwtUtil;


    @RequestMapping(path = "/login", method = RequestMethod.POST)
    @SneakyThrows
    private ResponseEntity<Map<String, String>> login(@RequestBody UserClient user) {

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        } catch (AuthenticationException e) {
            throw new Exception("Invalid login", e);
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails);

        final Map<String, String> response = new HashMap<>();
        response.put("token", jwt);

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}