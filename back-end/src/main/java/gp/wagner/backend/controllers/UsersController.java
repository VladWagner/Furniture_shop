package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.user.PasswordResetRequestDto;
import gp.wagner.backend.domain.dto.request.crud.user.PasswordUpdateRequestDto;
import gp.wagner.backend.domain.dto.request.crud.user.UserRequestDto;
import gp.wagner.backend.domain.dto.request.filters.UsersFilterRequestDto;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.dto.response.users.UserRespDto;
import gp.wagner.backend.domain.entites.users.User;
import gp.wagner.backend.infrastructure.Constants;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.middleware.Services;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.nio.file.Paths;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/users")
public class UsersController {

    // Получить список пользователей (только для админа/модератор)
    @GetMapping(value = "/all")
    public PageDto<UserRespDto> getAllUsers(@Valid @RequestPart(required = false) UsersFilterRequestDto filterDto,
                                            @Valid @RequestParam(value = "offset") @Max(100) int pageNum,
                                            @Valid @RequestParam(value = "limit") @Max(80) int limit) {

        Page<User> usersPage = Services.usersService.getAll(pageNum, limit);

        return new PageDto<>(
                usersPage,
                () -> usersPage.getContent().stream().map(UserRespDto::new).toList()
        );
    }

    // Получить список пользователей по заданному ключевому слову в имени или логине
    @GetMapping(value = "/find_by_keyword")
    public PageDto<UserRespDto> getUsersByKeyword(
            @RequestParam(value = "key") String key,
            @Valid @RequestPart(value = "filter", required = false) UsersFilterRequestDto filterDto,
            @Valid @RequestParam(value = "offset") @Max(100) int pageNum,
            @Valid @RequestParam(value = "limit") @Max(80) int limit) {

        Page<User> usersPage = Services.usersService.getByKeyword(key, filterDto, pageNum, limit);

        return new PageDto<>(
                usersPage,
                () -> usersPage.getContent().stream().map(UserRespDto::new).toList()
        );
    }


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

    // Повторная отправка сообщения для подтверждения на почту
    @GetMapping(value = "/resend_confirmation", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> resendConfirmationLetter(@RequestParam(value = "email") String email) throws Exception {

        User user = Services.usersService.resendConfirmationMessage(email);

        return ResponseEntity.ok(String.format("Сообщение для подтверждения повторно отправлено на '%s'!", user.getEmail()));
    }

    // Подтверждение почты
    @GetMapping(value = "/confirm", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> confirmUser(@RequestParam(value = "token") String token) throws Exception {

        User user = Services.usersService.confirmEmail(token);

        return ResponseEntity.ok(String.format("Почта '%s' успешно подтверждена!", user.getEmail()));
    }

    // Смена пароля
    @PutMapping(value = "/change_password")
    public ResponseEntity<Boolean> updatePassword(@Valid @RequestBody PasswordUpdateRequestDto passwordUpdateDto) {

        Services.usersService.updatePassword(passwordUpdateDto);

        return ResponseEntity.ok(true);
    }


    // Редактирование профиля
    @PutMapping(value = "/update")
    public ResponseEntity<UserRespDto> updateUser(@Valid @RequestPart(value = "user") UserRequestDto userDto,
                                                  @RequestPart(value = "profile_photo", required = false) MultipartFile file
    ) throws Exception {

        SimpleTuple<User, Boolean> tuple = Services.usersService.update(userDto);
        User updatedUser = tuple.getValue1();

        boolean generateImg = tuple.getValue2();
        URI oldImgUri = updatedUser.getProfilePhoto() != null ? new URI(updatedUser.getProfilePhoto()) : null;
        String oldImgFileName = oldImgUri != null ? Paths.get(oldImgUri).getFileName().toString() : "";

        // Если файл изображения пользователя задан
        if (file != null && !file.isEmpty()) {

            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            fileName = Utils.cleanUrl(Services.fileManageService.saveUserProfileImg(fileName, file, updatedUser.getId()).toString());

            updatedUser.setProfilePhoto(fileName);

            Services.usersService.update(updatedUser);

            Services.fileManageService.deleteFile(oldImgUri);

        }
        // Если файл загружен не был и при этом старое изображение является сгенерированным
        else if (generateImg && (oldImgFileName.contains(Constants.GENERATED_USER_IMG_CODE) || oldImgFileName.isBlank())) {

            String fileName = Utils.cleanUrl(Services.fileManageService.generateAndSaveUserImg(updatedUser).toString());

            Services.fileManageService.deleteFile(oldImgUri);

            updatedUser.setProfilePhoto(fileName);

            Services.usersService.update(updatedUser);

        }

        return ResponseEntity.ok(new UserRespDto(updatedUser));
    }

    // Удаление аватара пользователя
    @PutMapping(value = "/delete_photo/{user_id}")
    public ResponseEntity<String> deleteUserImg(@Valid @PathVariable(value = "user_id") long userId) throws Exception {

        User updatedUser = Services.usersService.getById(userId);

        // Получить URI и имя удаляемого изображения
        URI oldImgUri = updatedUser.getProfilePhoto() != null ? new URI(updatedUser.getProfilePhoto()) : null;
        String oldImgFileName = oldImgUri != null ? Paths.get(oldImgUri).getFileName().toString() : "";

        // Удалить старое фото, если оно не содержит идентификатора сгенерированного изображения и оно вообще задано
        if (!oldImgFileName.isBlank() && !oldImgFileName.contains(Constants.GENERATED_USER_IMG_CODE)) {

            // Сгенерировать новое изображение
            String fileName = Utils.cleanUrl(Services.fileManageService.generateAndSaveUserImg(updatedUser).toString());

            Services.fileManageService.deleteFile(oldImgUri);

            updatedUser.setProfilePhoto(fileName);

            Services.usersService.update(updatedUser);

        }

        return ResponseEntity.ok(updatedUser.getProfilePhoto());
    }

    // Проверка логина пользователя на уникальность
    // Если возвращается true, тогда вывести сообщение о том, что пользователь с таким логином уже существует
    @GetMapping(value = "/check_login")
    public ResponseEntity<Boolean> getLoginExists(@Valid @RequestParam(value = "val") String login) {

        boolean result = Services.usersService.userWithLoginExists(login);
        return ResponseEntity.ok(result);
    }

    // Проверка email пользователя на уникальность
    // Если возвращается true, тогда вывести сообщение о том, что пользователь с таким email уже зарегестрирован
    @GetMapping(value = "/check_email")
    public ResponseEntity<Boolean> getEmailExists(@Valid @RequestParam(value = "val") String email)  {

        if (Utils.emailIsValid(email))
            throw new ConstraintViolationException("Email пользователя задан некорректно!", null);

        boolean result = Services.usersService.userWithEmailExists(email);
        return ResponseEntity.ok(result);
    }

    // Отправка сообщения на сброс пароля
    @PostMapping(value = "/reset_password_request")
    public ResponseEntity<?> restPasswordRequest(@RequestBody String email) throws Exception {

        Services.usersService.createTokenForPasswordRecovery(email);

        return ResponseEntity.ok(String.format("На '%s' отправлено письмо с ссылкой для восстановления!", email));
    }

    // Замена пароля по отправленному на почту (на фронте отдельная страница с формой )
    @PostMapping(value = "/reset_password_by_token", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> saveResetPassword(@Valid @RequestBody PasswordResetRequestDto passwordResetRequestDto) throws Exception {

        User user = Services.usersService.savePasswordAfterReset(passwordResetRequestDto);

        return ResponseEntity.ok(String.format("Пароль для пользователя '%s' успешно сброшен!", user.getUserLogin()));
    }

    // АТОРИЗАЦИЯ пользователя через Spring security


}
