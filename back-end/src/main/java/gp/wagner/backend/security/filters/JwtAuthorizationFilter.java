package gp.wagner.backend.security.filters;

import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.domain.exceptions.classes.JwtValidationException;
import gp.wagner.backend.infrastructure.Constants;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.security.models.JwtAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Фильтр запроса для проверки наличия токена в заголовках
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private HandlerExceptionResolver exceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // Если маршрут входит в список игнорируемых
        if (isIgnoredRoute(request.getServletPath())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = getTokenFromHeader(request);

            if (token != null && Services.jwtService.validateAccessToken(token)) {

                JwtAuthentication authentication = Services.jwtService.getAuthentication(token);
                SecurityContext securityContext = SecurityContextHolder.getContext();

                securityContext.setAuthentication(authentication);
            }

            filterChain.doFilter(request, response);
        } catch (JwtValidationException | IOException | ServletException | AccessDeniedException e) {
            exceptionResolver.resolveException(request, response, null, e);
        }
    }

    // Метод возможно стоит вынести в infrastructure
    private String getTokenFromHeader(HttpServletRequest request){

        String authorizationHeader = request.getHeader(Constants.AUTHORIZATION_HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            String token = authorizationHeader.substring("Bearer ".length());

            return token;
        }

        return null;
    }

    // По каким маршрутам не нужно проверять наличие токенов в запросах
    private boolean isIgnoredRoute(String route){
        Set<String> ignoredRoutes = new HashSet<>(
                Arrays.asList(
                        "/api/users/register",
                        "/api/users/resend_confirmation",
                        "/api/users/confirm",
                        "/api/users/check_login",
                        "/api/users/check_email"
                )
        );

        return ignoredRoutes.contains(route);
    }
}
