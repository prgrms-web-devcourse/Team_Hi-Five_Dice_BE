package com.cocodan.triplan.jwt;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.cocodan.triplan.config.JwtAuthenticationSuccessHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.log.LogMessage;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class JwtAuthenticationFilter extends GenericFilterBean {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String headerKey;

    private final Jwt jwt;

    private final JwtAuthenticationSuccessHandler successHandler = new JwtAuthenticationSuccessHandler();

    public JwtAuthenticationFilter(String headerKey, Jwt jwt) {
        this.headerKey = headerKey;
        this.jwt = jwt;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            String token = getToken(request);
            if (token != null) {
                try {
                    Jwt.Claims claims = verify(token);
                    log.debug("Jwt parse result: {}", claims);

                    Long id = claims.id;
                    List<GrantedAuthority> authorities = getAuthorities(claims); // 꺼내오는 코드

                    if (id != 0 && !authorities.isEmpty()) {
                        JwtAuthenticationToken authentication =
                                new JwtAuthenticationToken(new JwtAuthentication(token, id), null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        successfulAuthentication(request, response, chain, authentication);
                    }
                } catch (Exception e) {
                    log.warn("Exception type: {}", e.getClass().getSimpleName());
                    log.warn("Jwt processing failed: {}", e.getMessage());
                    if (e.getClass() == TokenExpiredException.class)
                    {
                        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                        throw new TokenExpiredException("Login Token has expired!");
                    }
                }
            }
        } else {
            log.debug("SecurityContextHolder not populated with security token, as it already contained: '{}'",
                    SecurityContextHolder.getContext().getAuthentication());
        }

        chain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest request) {
        String token = request.getHeader(headerKey);
        if (isNotEmpty(token)) {
            log.debug("Jwt authorization api detected: {}", token);
            try {
                return URLDecoder.decode(token, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error(e.getMessage(), e);
            }
        }
        return null;
    }

    private Jwt.Claims verify(String token) {
        return jwt.verify(token);
    }

    private List<GrantedAuthority> getAuthorities(Jwt.Claims claims) {
        String[] roles = claims.roles;
        return roles == null || roles.length == 0 ?
                emptyList() :
                Arrays.stream(roles).map(SimpleGrantedAuthority::new).collect(toList());
    }

    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
                                            Authentication authResult) throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        if (this.logger.isDebugEnabled()) {
            this.logger.debug(LogMessage.format("Set SecurityContextHolder to %s", authResult));
        }
        this.successHandler.onAuthenticationSuccess(request, response, authResult);
    }
}