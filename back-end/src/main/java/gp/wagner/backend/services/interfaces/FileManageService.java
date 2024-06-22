package gp.wagner.backend.services.interfaces;

import gp.wagner.backend.configurations.FileUploadProperties;
import gp.wagner.backend.domain.entities.reviews.Review;
import gp.wagner.backend.domain.entities.users.User;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;

//Отдельный сервис для работы с изображениями
public interface FileManageService {

    FileUploadProperties getFilesPaths();

    //Загрузить файл варианта товара
    Resource savePvImgFile(String fileName, MultipartFile multipartFile, Long categoryId, Long productId, Long productVariantId) throws IOException;

    //Загрузить файл для базового варианта товара
    Resource saveProductImgFile(String fileName, MultipartFile multipartFile, Long categoryId, Long productId) throws IOException;

    // Загрузить изображение только для производителя
    Resource saveProducerThumb(String fileName, MultipartFile multipartFile, Long producerId) throws IOException;
    // Загрузить изображение только для категории
    Resource saveCategoryThumb(String fileName, MultipartFile multipartFile, Long categoryId, boolean isRepeating) throws IOException;

    //Сформировать файл предосмотра
    Resource saveThumbnail(String filePath, Long categoryId, Long productId) throws IOException ;

    // Загрузить изображение профиля пользователя
    Resource saveUserProfileImg(String fileName, MultipartFile multipartFile, long userId) throws IOException;

    // Загрузить изображение отзыва
    Resource saveReviewImg(String fileName, MultipartFile multipartFile, Review review) throws IOException;

    // Сформировать и загрузить изображение профиля пользователя
    Resource generateAndSaveUserImg(User user) throws IOException;

    //Удалить файл
    void deleteFile(URI filePath) throws IOException;

    String renameFile(String filePath, String newName);

    //Проверить, существует ли файл
    boolean isExists(URI filePath);


}
