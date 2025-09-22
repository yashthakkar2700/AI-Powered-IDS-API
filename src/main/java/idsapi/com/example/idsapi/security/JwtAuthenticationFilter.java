package idsapi.com.example.idsapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        System.out.println("JwtAuthenticationFilter running");
        System.out.println("REquest:");
        System.out.println(request);


        String authHeader = request.getHeader("Authorization");

        System.out.println("JwtAuthenticationFilter::doFilterInternal(): Auth header: " + authHeader);


        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            String username = jwtService.validateTokenAndGetUsername(token);

            System.out.println("JWT username: " + username);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Assign a dummy role (ROLE_USER)
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                username,
                                null,
                                List.of(() -> "ROLE_USER")  // Dummy authority
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            System.out.println("Authentication exists: " + SecurityContextHolder.getContext().getAuthentication().getName());
        } else {
            System.out.println("No authentication yet!!!");
        }

        filterChain.doFilter(request, response);
    }
}
