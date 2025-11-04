package com.soen343.tbd.infrastructure.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    // to handle the unauthorized access by redirecting to a specific entry point
    // 401
    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    // to filter and validate JWT tokens in incoming requests

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    // to encode passwords using BCrypt hashing algorithm
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // to manage authentication processes

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    // to configure security settings for HTTP requests
    // only allow the login and register endpoints without authentication, every
    // other endpoint requires authentication

    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authz -> authz
                        // the only allowed endpoints without authentication ( can be accessed without a
                        // token)
                        .requestMatchers("/api/login", "/api/register").permitAll()
                        .requestMatchers("/api/stations/stream", "/api/stations/subscribe").permitAll()
                        // all other endpoints require authentication
                        .anyRequest().authenticated())
                // If any exception occurs, this will handle it by redirecting to 401
                .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        // Add a filter to validate the tokens with every request. It forces tgat tge
        // filter is executed before the UsernamePasswordAuthenticationFilter
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // to configure CORS settings for the application. Instead of at each time
    // telling the backend the react request is coming from localhost3000, we do it
    // once here
    @Bean
    // define the CORS configuration source ( so by default accept all requests from
    // localhost3000)
    // this is a fallback, so in case the frontend forgets to add the origin in the
    // request, the backend will still accept it thanks to this configuration
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // do that for any endpoint in the backend
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
