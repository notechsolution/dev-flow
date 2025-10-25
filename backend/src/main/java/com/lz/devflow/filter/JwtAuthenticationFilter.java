package com.lz.devflow.filter;

import com.lz.devflow.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = org.slf4j.LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Resource
    private JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String jwt = getJwtFromRequest(request);
        if (StringUtils.hasText(jwt) && jwtService.validateToken(jwt)) {
            Claims claims = jwtService.extractClaimsFromJwt(jwt);
            List<GrantedAuthority> authorities = new ArrayList<>();
            // loop audience and add as authorities
            Set<String> audience = claims.getAudience();
            if (audience != null) {
                for (String role : audience) {
                    authorities.add(new SimpleGrantedAuthority(String.format("ROLE_%s", role)));
                }
            }
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    claims.getSubject(), null, authorities);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            log.warn("Invalid jwt token {}", jwt);
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getRequestURI().contains("/api/webhook") &&
                !request.getRequestURI().contains("/api/graph");
    }

    /**
     * Extracts the token from the HttpServletRequest.
     * The method checks for the token in the following order:
     * 1. Authorization header with Bearer token.
     * 2. x-auth-token header.
     * 3. Query parameter named "token".
     *
     * @param request the HttpServletRequest object containing the client request
     * @return the extracted token, or null if no token is found
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        String xAuthToken = request.getHeader("x-auth-token");
        if (StringUtils.hasText(xAuthToken)) {
            return xAuthToken;
        }
        return request.getParameter("token");
    }

}
