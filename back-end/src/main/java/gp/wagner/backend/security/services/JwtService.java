package gp.wagner.backend.security.services;

import gp.wagner.backend.domain.entities.users.User;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.domain.exceptions.classes.JwtValidationException;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.repositories.tokens.RefreshTokensRepository;
import gp.wagner.backend.security.models.JwtAuthentication;
import gp.wagner.backend.security.models.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {

    //@Autowired
    private Environment env;
    private final SecretKey accessJwtSecretKey;
    private final SecretKey refreshJwtSecretKey;

    // Сроки жизни токенов в миллисекундах
    private final Long accessTokenLifeTimeMs;
    private final Long refreshTokenLifeTimeMs;

    private final SignatureAlgorithm algorithm = SignatureAlgorithm.HS256;

    public JwtService(@Autowired Environment envInjected) {

        this.env = envInjected;

        this.accessJwtSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(env.getProperty("spring.jwt.secret.access")));
        this.refreshJwtSecretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(env.getProperty("spring.jwt.secret.refresh")));

        this.accessTokenLifeTimeMs = Utils.TryParseLong(env.getProperty("spring.jwt.life-time.access"));
        this.refreshTokenLifeTimeMs = Utils.TryParseLong(env.getProperty("spring.jwt.life-time.refresh"));
    }

    // Репозиторий refresh токенов
    private RefreshTokensRepository refreshTokensRepository;

    @Autowired
    public void setRefreshTokensRepository(RefreshTokensRepository repository) {
        this.refreshTokensRepository = repository;
    }

    // Сгенерировать access токен
    public String generateAccessToken(User user) {

        if (user == null)
            throw new ApiException("Не удалось создать access токен. Параметр задан некорректно!");

        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        // Что задавать в claims: id пользователя, user_name, роль (либо списком), флаг подтверждения почты
        Claims claims = Jwts.claims().setSubject(user.getUserLogin());
        claims.put("user_id", user.getId());
        claims.put("is_confirmed", user.getIsConfirmed());

        // Задать роли здесь, чтобы при каждом запросе не обращаться в БД
        claims.put("roles", userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).toArray());

        // Задать дату истечения срока действия токена
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + accessTokenLifeTimeMs);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(accessJwtSecretKey, algorithm)
                .compact();
    }

    // Сгенерировать refresh токен
    public String generateRefreshToken(User user) {

        if (user == null)
            throw new ApiException("Не удалось создать refresh токен. Параметр задан некорректно!");

        // Что задавать в claims: id пользователя (смотри на валидацию токена)
        Claims claims = Jwts.claims().setSubject(user.getUserLogin());
        claims.put("user_id", user.getId());

        // Задать дату истечения срока действия токена
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + refreshTokenLifeTimeMs);

        String token = Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expirationDate).signWith(refreshJwtSecretKey, algorithm)
                .compact();

        refreshTokensRepository.insert(token, user.getId(), expirationDate);

        return token;
    }

    // Получить объект для аутентификации из access токена
    public JwtAuthentication getAuthentication(Claims claims) {

        List<SimpleGrantedAuthority> authorities = claims.get("roles", ArrayList.class)
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.toString()))
                .toList();

        UserDetailsImpl userDetails = new UserDetailsImpl(authorities, claims.get("user_id", Long.class), claims.getSubject(),
                claims.get("is_confirmed", Boolean.class), null);

        return new JwtAuthentication(userDetails, true);
    }

    public JwtAuthentication getAuthentication(String token) {

        return getAuthentication(getClaims(token, accessJwtSecretKey));
    }


    // Получить объект для аутентификации из access токена с обращением к БД
    public JwtAuthentication getAuthenticationWithDb(Claims claims) {

        User user = Services.usersService.getById((Long) claims.get("user_id"));

        return new JwtAuthentication(user, true);
    }
    public JwtAuthentication getAuthenticationWithDb(String token) {
        return getAuthenticationWithDb(getClaims(token, accessJwtSecretKey));
    }

    // Валидация refresh токена
    public boolean validateRefreshToken(String token) {

        Claims claims = getRefreshTokenClaims(token);

        // Дата истечения срока дейстия токена
        boolean isNotExpired = claims.getExpiration().after(new Date());


        boolean tokenExitsInTable = refreshTokensRepository.findByTokenAndUser(token, claims.get("user_id", Long.class)).orElse(null) != null;

        return isNotExpired && tokenExitsInTable;
    }

    // Валидация access токена
    public boolean validateAccessToken(String token) {

        Claims claims = getAccessTokenClaims(token);

        return claims.getExpiration().after(new Date());
    }

    // Общий метод получения claims токена
    private Claims getClaims(String token, SecretKey secretKey) {
        try {

            // Спарсить токен с проверкой подписи
            Jws<Claims> jwtClaims = Jwts.parserBuilder().setSigningKey(secretKey).build().parseClaimsJws(token);

            return jwtClaims.getBody();

        } catch (ExpiredJwtException | UnsupportedJwtException | MalformedJwtException | IllegalArgumentException |
                 SignatureException e) {
            throw new JwtValidationException(e.getLocalizedMessage());
        }
    }

    public Claims getRefreshTokenClaims(String token){
        return getClaims(token, refreshJwtSecretKey);
    }

    public Claims getAccessTokenClaims(String token){
        return getClaims(token, accessJwtSecretKey);
    }


}
