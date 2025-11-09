package com.serva.gestionrestaurante.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**", "/login", "/error/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/mesas/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/menu", "/menu/**").hasAnyRole("ADMIN","MOZO","CLIENTE")
                        .requestMatchers("/menu/**").hasRole("ADMIN")
                        .requestMatchers("/insumos/**").hasRole("ADMIN")
                        .requestMatchers("/clientes/**", "/mesas/**").hasAnyRole("ADMIN","MOZO")
                        .requestMatchers("/api/mesas/**", "/api/clientes/**").hasAnyRole("ADMIN","MOZO")
                        .requestMatchers("/api/**").hasRole("ADMIN")
                        .requestMatchers("/", "/dashboard").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(l -> l
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .exceptionHandling(e -> e.accessDeniedPage("/error/403"));

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(BCryptPasswordEncoder encoder) {
        UserDetails cliente = User.withUsername("cliente@demo.com")
                .password(encoder.encode("12345"))
                .roles("CLIENTE")
                .build();

        UserDetails mozo = User.withUsername("mozo")
                .password(encoder.encode("12345"))
                .roles("MOZO")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("12345"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(cliente, mozo, admin);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
