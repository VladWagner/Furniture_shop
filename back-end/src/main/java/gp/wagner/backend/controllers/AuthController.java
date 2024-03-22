package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.auth.AuthDto;
import gp.wagner.backend.domain.dto.request.auth.JwtRequestDto;
import gp.wagner.backend.domain.dto.request.crud.user.UserRequestDto;
import gp.wagner.backend.domain.dto.response.JwtRespDto;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.middleware.Services;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

@RestController
@RequestMapping(value = "/api/auth")
public class AuthController {

    // Регистрация пользователя
    @PostMapping(value = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> registerUser(
            @Valid @RequestPart(value = "user") UserRequestDto userDto,
            @RequestPart(value = "profile_photo", required = false) MultipartFile file
    ) throws Exception {

        User createdUser;

        createdUser = Services.usersService.create(userDto);

        String fileName;

        // Если файл изображения пользователя задан
        if (file != null && !file.isEmpty()) {

            fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            fileName = Utils.cleanUrl(Services.fileManageService.saveUserProfileImg(fileName, file, createdUser.getId()).toString());

        } else {

            fileName = Utils.cleanUrl(Services.fileManageService.generateAndSaveUserImg(createdUser).toString());

        }

        createdUser.setProfilePhoto(fileName);
        Services.usersService.update(createdUser);

        return ResponseEntity.ok(String.format("На почту '%s' отправлено сообщение для подтверждения аккаунта",createdUser.getEmail()));
    }

    // Сгенерировать новый access токен по refresh токену, передаваемому в cookies
    @GetMapping(value = "/get_access_token")
    public ResponseEntity<?> getAccessToken(HttpServletRequest request) {

        JwtRespDto respDto = Services.authService.getAccessToken(Utils.readCookie(request, "refresh_token"));

        return ResponseEntity.ok(respDto);
    }

    // Аутентификация пользователя
    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<JwtRespDto> login(@RequestBody AuthDto authDto) throws Exception {

        JwtRespDto jwtRespDto = Services.authService.login(authDto);

        return ResponseEntity.ok(jwtRespDto);
    }

    // Выйти из авторизированного состояния
    @PostMapping(value = "/logout", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> logout(@RequestBody JwtRequestDto requestDto) throws Exception {

        Services.authService.logout(requestDto.getRefreshToken());

        boolean isLoggedOut = SecurityContextHolder.getContext() == null;

        return ResponseEntity
                .status(isLoggedOut ? HttpStatus.OK : HttpStatus.NOT_MODIFIED)
                .body(String.format("Выход произошел %s", isLoggedOut ? "успешно" : "неудачно"));
    }

    // Выйти из авторизированного состояния на всех устройствах и браузерах
    @PostMapping(value = "/logout_all_sessions")
    public ResponseEntity<String> logoutAll(@RequestBody JwtRequestDto requestDto) throws Exception {

        Services.authService.logoutAllUserSessions();

        boolean isLoggedOut = SecurityContextHolder.getContext() == null;

        return ResponseEntity
                .status(isLoggedOut ? HttpStatus.OK : HttpStatus.NOT_MODIFIED)
                .body(String.format("Выход произошел %s", isLoggedOut ? "успешно" : "неудачно"));
    }

}
