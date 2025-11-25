package org.hsdd.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.hsdd.domain.User;
import org.hsdd.repo.UserRepository;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class TokenAuthFilter extends OncePerRequestFilter {

    private final UserRepository users;

    public TokenAuthFilter(UserRepository users) {
        this.users = users;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain
    ) throws ServletException, IOException {

        String authHeader = req.getHeader("Authorization");

        // ⭐ FIX: support lowercase header (happens in PUT/POST JSON requests)
        if (authHeader == null) {
            authHeader = req.getHeader("authorization");
        }

        if (authHeader != null && authHeader.startsWith("Bearer TOKEN-")) {
            try {
                Long userId = Long.parseLong(authHeader.substring("Bearer TOKEN-".length()));

                User u = users.findById(userId).orElse(null);

                if (u != null && u.isActive()) {

                    String springRole = "ROLE_" + u.getRole().toUpperCase();

                    Authentication auth = new UsernamePasswordAuthenticationToken(
                            u.getUsername(),
                            null,
                            List.of(new SimpleGrantedAuthority(springRole))
                    );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }

            } catch (Exception ignored) {}
        }

        chain.doFilter(req, res);
    }

}
