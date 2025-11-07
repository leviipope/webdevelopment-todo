package com.leviipope.todoapp.security;

import com.leviipope.todoapp.service.TokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import java.util.Collections;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;

    public JwtFilter(TokenService tokenService, UserDetailsService userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/h2/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            System.out.println("No Authorization header or invalid format");
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        String username = tokenService.extractUsername(token);

        System.out.println("JWT Filter - Username from token: " + username);
        System.out.println("JWT Filter - Request URI: " + request.getRequestURI());

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                System.out.println("JWT Filter - User found in database: " + userDetails.getUsername());

                if (tokenService.validateToken(token, userDetails)) {
                    System.out.println("JWT Filter - Token is valid");
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.setContext(SecurityContextHolder.createEmptyContext());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    System.out.println("JWT Filter - Token validation failed");
                }
            } catch (Exception e) {
                System.out.println("JWT Filter - Error loading user: " + e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
