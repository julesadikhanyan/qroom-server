package com.qroom.common.security.config;

import com.qroom.common.security.jwtFilters.JwtAuthenticationFilter;
import com.qroom.common.security.jwtFilters.JwtRefreshFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtRefreshFilter jwtRefreshFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, JwtRefreshFilter jwtRefreshFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtRefreshFilter = jwtRefreshFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .cors()
                //.configurationSource(request -> corsConfiguration)
                .and()
                .csrf().disable()
                .authorizeRequests()
                .antMatchers(HttpMethod.GET, "/rooms**").permitAll()
                .antMatchers(HttpMethod.GET, "/rooms/**").permitAll()
                .antMatchers(HttpMethod.GET, "/rooms/booking/**").permitAll()
                .antMatchers(HttpMethod.POST, "/users/register").permitAll()
                .antMatchers(HttpMethod.POST, "/users/authenticate").permitAll()
                .antMatchers(HttpMethod.PUT, "/users/authenticate/refresh").hasAuthority("REFRESH")
                .antMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .antMatchers(HttpMethod.OPTIONS).permitAll()
                .anyRequest().hasAuthority("ACCESS")
                .and()
                .addFilterAt(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(jwtRefreshFilter, UsernamePasswordAuthenticationFilter.class)
                .headers().frameOptions().sameOrigin();
    }
}
