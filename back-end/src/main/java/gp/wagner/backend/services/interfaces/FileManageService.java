package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.domain.entites.users.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

//Отдельный сервис для работы с изображениями
public interface FileManageService {

    //Загрузить файл варианта товара
    Resource saveFile(String fileName, MultipartFile multipartFile, Long categoryId, Long productId, Long productVariantId) throws IOException;

    //Загрузить файл для базового варианта товара
    Resource saveFile(String fileName, MultipartFile multipartFile, Long categoryId, Long productId) throws IOException;

    // Загрузить изображение категории или производителя
    Resource saveProducerOrCategoryThumb(String fileName, MultipartFile multipartFile, Long categoryId, Long producerId) throws IOException;

    //Сформировать файл предосмотра
    Resource saveThumbnail(String filePath, Long categoryId, Long productId) throws IOException ;

    // Загрузить изображение профиля пользователя
    Resource saveUserProfileImg(String fileName, MultipartFile multipartFile, long userId) throws IOException;

    // Сформировать и загрузить изображение профиля пользователя
    Resource generateAndSaveUserImg(User user) throws IOException;

    //Удалить файл
    void deleteFile(URI filePath) throws IOException;

    //Проверить, существует ли файл
    boolean isExists(URI filePath);


}
