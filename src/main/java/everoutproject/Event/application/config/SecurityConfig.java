package everoutproject.Event.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        return handler;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        .requestMatchers("/", "/homepage", "/user", "/events").permitAll()

                        .requestMatchers(
                                "/api/users/login",
                                "/api/users/create",
                                "/api/events",
                                "/api/events/image/**",
                                "/api/bookings/create"
                        ).permitAll()

                        .requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/users", "/api/users/**").hasRole("ADMIN")

                        .requestMatchers("/api/events/create", "/api/events/edit/**", "/api/events/cancel/**")
                        .hasAnyRole("ADMIN", "BACKOFFICE")

                        .requestMatchers("/api/organizers", "/api/organizers/**").permitAll()

                        .requestMatchers("/api/bookings").hasAnyRole("ADMIN", "BACKOFFICE", "FRONTOFFICE", "USER")

                        .anyRequest().authenticated())

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        return http.build();
    }
}
