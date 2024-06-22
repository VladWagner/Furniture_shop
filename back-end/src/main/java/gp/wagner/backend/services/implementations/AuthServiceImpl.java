package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.dto.request.auth.AuthDto;
import gp.wagner.backend.domain.dto.response.JwtRespDto;
import gp.wagner.backend.domain.entities.tokens.RefreshToken;
import gp.wagner.backend.domain.entities.users.User;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.ServicesUtils;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.tokens.RefreshTokensRepository;
import gp.wagner.backend.services.interfaces.AuthService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private PasswordEncoder passwordEncoder;

    @Autowired
    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    // Репозиторий refresh токенов
    private RefreshTokensRepository refreshTokensRepository;

    @Autowired
    public void setRefreshTokensRepository(RefreshTokensRepository repository) {
        this.refreshTokensRepository = repository;
    }

    SecurityContext securityContext;

    public AuthServiceImpl() {
        this.securityContext = SecurityContextHolder.getContext();
    }

    @Override
    public JwtRespDto login(AuthDto dto) {

        // Осуществить поиск по username или email
        boolean findByEmail = Utils.emailIsValid(dto.getLogin());

        User user = findByEmail ? Services.usersService.getByEmailNullable(dto.getLogin()) : Services.usersService.getByLoginNullable(dto.getLogin())
                .orElse(null);

        // Если пользователя найти не удалось или его учётная запись не подтверждена
        if (user == null)
            throw new UsernameNotFoundException(String.format("Пользователь с %s %s не найден!", findByEmail ? "email" : "username", dto.getLogin()));


        if (!passwordEncoder.matches(dto.getPassword(), user.getUserPassword().getPassword()))
            throw new ApiException("Пароль введён неверно!",HttpStatus.FORBIDDEN);
        else if (!user.getIsConfirmed())
            throw new ApiException(
                    String.format("Не удалось пройти аутентификацию! Почта пользователя %s не подтверждена.", user.getUserLogin()),
                    HttpStatus.FORBIDDEN
            );

        return new JwtRespDto(Services.jwtService.generateAccessToken(user), Services.jwtService.generateRefreshToken(user));
    }

    @Override
    public void logout(String refreshToken) {

        RefreshToken token = refreshTokensRepository.findByToken(refreshToken).orElse(null);

        // Проверить, что токен принадлежит пользователю, который сейчас авторизирован (без аутентификации и авторизации вызвать logout нельзя)
        if (token != null){

            User user = ServicesUtils.getUserFromSecurityContext(securityContext);

            // Если не удалось получить авторизированного пользователя или в записи refresh токена задан другой пользователь
            if (user == null || !user.isEqualTo(token.getUser()))
                return;

            refreshTokensRepository.delete(token);

        }

        SecurityContextHolder.clearContext();

    }

    @Override
    public void logoutAllUserSessions() {

        // Получить авторизированного пользователя
        User user = ServicesUtils.getUserFromSecurityContext(securityContext);

        if (user == null)
            return;

        // Удалить все токены для данного пользователя (в будущем можно поменять на установку флага revoked)
        refreshTokensRepository.deleteAllByUserId(user.getId());

        SecurityContextHolder.clearContext();

    }

    @Override
    public JwtRespDto getAccessToken(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank())
            throw new ApiException("Не удалось сгенерировать новый access jwt! Refresh токен задан некорректно.");

        if (!Services.jwtService.validateRefreshToken(refreshToken))
            return null;

        Claims claims = Services.jwtService.getRefreshTokenClaims(refreshToken);

        User user = Services.usersService.getById(claims.get("user_id", Long.class));

        String accessToken = Services.jwtService.generateAccessToken(user);

        return new JwtRespDto(accessToken, null);
    }

    @Override
    public JwtRespDto getNewRefreshToken(String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank() || !Services.jwtService.validateRefreshToken(refreshToken))
            return null;

        Claims claims = Services.jwtService.getRefreshTokenClaims(refreshToken);

        User user = Services.usersService.getById(claims.get("user_id", Long.class));

        String newRefreshToken = Services.jwtService.generateRefreshToken(user);

        return new JwtRespDto(null, newRefreshToken);
    }

    // Запуск в 00:03 каждого дня
    //@Scheduled(fixedDelayString = "PT02M")
    @Scheduled(cron = "0 3 0 * * *")
    public void removeExpiredRefreshTokens() {
        refreshTokensRepository.deleteExpiredTokens();
        System.out.println("\n\tОтработал метод удаления всех просроченных токенов\n\n");
    }
}
