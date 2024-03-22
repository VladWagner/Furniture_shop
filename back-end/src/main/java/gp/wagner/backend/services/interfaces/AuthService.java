package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.dto.request.auth.AuthDto;
import gp.wagner.backend.domain.dto.response.JwtRespDto;
import org.springframework.scheduling.annotation.Async;

public interface AuthService {

    // Аутентификация пользователя
    JwtRespDto login(AuthDto dto);

    // Выход аутентифицированного и авторизированного пользователя - отзываем конкретный refresh токен (их может быть несколько)
    void logout(String refreshToken);

    // Выход из всех сессий (с других браузеров и мобильных устройств)
    void logoutAllUserSessions();

    // Получение access токена
    JwtRespDto getAccessToken(String refreshToken);

    // Получение access токена
    JwtRespDto getNewRefreshToken(String refreshToken);

    // Асинхронное фоновое удаление всех просроченных refresh токенов
    @Async
    void removeExpiredRefreshTokens();

}
