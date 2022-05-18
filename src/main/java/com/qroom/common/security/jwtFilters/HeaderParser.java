package com.qroom.common.security.jwtFilters;

import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;

public class HeaderParser {
    public static String retrieveToken(HttpServletRequest request) {
        String authHeader =  request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            return null;
        }
        if (!authHeader.startsWith("Bearer ")){
            System.out.println("Dont start with 'Bearer '");
            return null;
        }
        return authHeader.substring("Bearer ".length());
    }
}
