package gp.wagner.backend.security.filters;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.security.web.util.ThrowableCauseExtractor;
import org.springframework.web.filter.GenericFilterBean;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

@NoArgsConstructor
@AllArgsConstructor
public class AuthExceptionsHandlingFilter extends GenericFilterBean {

    private HandlerExceptionResolver exceptionResolver;
    private final ThrowableAnalyzer analyzer = new CustomThrowableAnalyzer();
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {

            // Получить массив исключений-предшественников обрабатываемого Exception
            Throwable[] exceptionsChain = analyzer.determineCauseChain(e);

            AuthenticationException authenticationException = ((AuthenticationException) analyzer.getFirstThrowableOfType(AuthenticationException.class, exceptionsChain));
            AccessDeniedException accessDeniedException = ((AccessDeniedException) analyzer.getFirstThrowableOfType(AccessDeniedException.class, exceptionsChain));

            // Exception resolver позволит отработать обработке ошибок в controllerAdvice в едином формате с остальными исключениями
            if (authenticationException != null)
                exceptionResolver.resolveException((HttpServletRequest) request, (HttpServletResponse) response, null, authenticationException);
            else if (accessDeniedException != null)
                exceptionResolver.resolveException((HttpServletRequest) request, (HttpServletResponse) response, null, accessDeniedException);
            else
                throw e;

        }

    }

    // Обработать исключение возникающее в случае обращения к маршруту, требующему аутентификации
    private void handleAuthenticationException(HttpServletRequest request, HttpServletResponse response, RuntimeException authException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Записать сообщение
        try (OutputStream os = response.getOutputStream()) {
            os.write(String.format("Для доступа к данному ресурсу нужна авторизация. %s", authException.getMessage()).getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }

    // Обработать исключение возникающее при попытке обращения пользователя с недостаточными правами к маршруту
    public void handleAccessDeniedException(HttpServletRequest request, HttpServletResponse response, RuntimeException accessDeniedException) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        // Записать сообщение
        try (OutputStream os = response.getOutputStream()) {
            os.write(String.format("У вас недостаточно прав. %s", accessDeniedException.getMessage()).getBytes(StandardCharsets.UTF_8));
            os.flush();
        }
    }
}

class CustomThrowableAnalyzer extends ThrowableAnalyzer{
    protected void initExtractorMap() {
        super.initExtractorMap();

        // Реализовать функциональный интерфейс извлекателя исключений вызвавших ServletException
        ThrowableCauseExtractor extractor = throwable -> {
            ThrowableAnalyzer.verifyThrowableHierarchy(throwable, ServletException.class);
            return ((ServletException) throwable).getRootCause();
        };

        registerExtractor(ServletException.class, extractor);
    }
}