package com.serva.gestionrestaurante.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity // habilita @PreAuthorize en el controlador
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // estáticos y página de login
                        .requestMatchers("/css/**","/js/**","/images/**","/webjars/**","/login").permitAll()
                        // vistas públicas autenticadas (listar/ver)
                        .requestMatchers(HttpMethod.GET, "/", "/clientes", "/clientes/ver/**").authenticated()
                        // todo lo de crear/editar/eliminar solo ADMIN
                        .requestMatchers("/clientes/nuevo", "/clientes/guardar", "/clientes/editar/**", "/clientes/eliminar/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")                         // usamos nuestra vista login.html
                        .defaultSuccessUrl("/clientes", true)        // SIEMPRE enviamos a /clientes
                        .failureUrl("/login?error")                  // si falla, vuelve a /login con error
                        .permitAll()
                )
                .logout(l -> l
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsService(BCryptPasswordEncoder encoder) {
        UserDetails user = User.withUsername("user")
                .password(encoder.encode("12345"))
                .roles("USER")
                .build();

        UserDetails admin = User.withUsername("admin")
                .password(encoder.encode("12345"))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
