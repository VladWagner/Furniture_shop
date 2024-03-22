package gp.wagner.backend.infrastructure;

import gp.wagner.backend.domain.entites.users.UserRole;
import gp.wagner.backend.middleware.Services;

import java.nio.file.Path;
import java.nio.file.Paths;

//Общие константные переменные по проекту
public class Constants {

    public static String UPLOAD_URI = "uploads/";
    public static Path UPLOAD_PATH_IMG = Paths.get(UPLOAD_URI,"images");
    public static Path UPLOAD_PATH_UTIL = Paths.get(UPLOAD_URI,"utils");
    public static Path UPLOAD_PATH_USERS = Paths.get(UPLOAD_URI,"users");
    public static Path UPLOAD_PATH_REVIEWS = Paths.get(UPLOAD_URI,"reviews");
    public static Path UPLOAD_PATH_THUMB = Paths.get(UPLOAD_URI,"thumbnails");
    public static String THUMB_SUFFICE = "-thumb";

    public static Path WATERMARK_PATH = Paths.get(UPLOAD_URI,"water-mark.png");

    public static Path EMPTY_IMAGE = Paths.get(UPLOAD_URI,"empty.jpg");

    // Данное значение получилось после манипуляций с порядковыми номерами символов строки "generated"
    public static String GENERATED_USER_IMG_CODE = "g408262208";
    public static String EMAIL_REG_EXP = "^[_A-Za-z0-9-+]+(.[_A-Za-z0-9-]+)*@+[A-Za-z0-9-]+(.[A-Za-z0-9]+)*(.[A-Za-z]{2,})$";

    // Id статуса mutable заказа
    public static int MutableOrderStateId = 1;

    // Id статуса mutable заказа
    public static float PRODUCT_ATTR_PRIORITY_INCREMENT = 0.1f;

    // Начальная роль пользователя
    public static UserRole BASIC_USER_ROLE = Services.usersService.getBasicUserRole();

    public static String AUTHORIZATION_HEADER = "Authorization";

}
