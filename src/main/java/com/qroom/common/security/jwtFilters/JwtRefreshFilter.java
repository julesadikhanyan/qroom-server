package com.qroom.common.security.jwtFilters;

import com.qroom.common.security.UserAuth;
import com.qroom.common.security.managers.JwtTokenManager;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtRefreshFilter extends OncePerRequestFilter {
    private final JwtTokenManager manager;

    public JwtRefreshFilter(JwtTokenManager manager) {
        this.manager = manager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {
        String token = HeaderParser.retrieveToken(request);
        if (token == null) {
            System.out.println("no token auth");
            filterChain.doFilter(request, response);
            return;
        }
        try {
            UserAuth userAuth = manager.readRefreshToken(token);
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(
                            userAuth,
                            token,
                            userAuth.getAuthorities()
                    )
            );
        }
        catch (IllegalArgumentException ignored){

        }
        catch (ExpiredJwtException exception) {
            try {
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Jwt token expired");
            }
            catch (Exception ignore){
                System.out.println("Dermo here");
            }
            return;
        }
        filterChain.doFilter(request, response);
    }
}
