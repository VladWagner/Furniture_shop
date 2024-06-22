package gp.wagner.backend.security.filters;

import gp.wagner.backend.infrastructure.Utils;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsHeadersFilter implements Filter {



    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        //response.setHeader("Access-Control-Allow-Origin", "http://localhost:3000");

        // Домен источника запроса
        String origin = request.getHeader("Origin");

        // Если домен есть в списке разрешенных, тогда задать его в заголовке ответа
        if (origin != null && Utils.corsAllowedOrigins.contains(origin))
            response.setHeader("Access-Control-Allow-Origin", origin);

        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Credentials", "true");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Methods", "*");

        filterChain.doFilter(req, resp);
    }
}
