package gp.wagner.backend.services.interfaces;

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

    //Сформировать файл предосмотра
    Resource saveThumbnail(String filePath, Long categoryId, Long productId) throws IOException ;

    //Удалить файл
    void deleteFile(URI filePath) throws IOException;

    //Проверить, существует ли файл
    boolean isExists(URI filePath);


}
