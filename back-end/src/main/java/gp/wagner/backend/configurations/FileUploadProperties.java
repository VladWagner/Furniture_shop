package gp.wagner.backend.configurations;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Data
public class FileUploadProperties {

    @Value("${spring.files.uploads-path}")
    private String uploadsPath;

    // Путь для загрузки изображений товаров
    public Path uploadImgPath(){
        return Paths.get(uploadsPath,"images");
    }

    // Путь для загрузки utils изображений
    public Path uploadUtilsPath(){
        return Paths.get(uploadsPath,"utils");
    }

    // Путь для загрузки изображений пользователей
    public Path uploadUsersPhotosPath(){
        return Paths.get(uploadsPath,"users");
    }

    // Путь для загрузки изображений в отзывах
    public Path uploadReviewsPath(){
        return Paths.get(uploadsPath,"reviews");
    }

    // Путь для загрузки превью товаров
    public Path uploadThumbsPath(){
        return Paths.get(uploadsPath,"thumbnails");
    }

    // Путь к водному знаку
    public Path watermarkPath(){
        return Paths.get(uploadsPath,"water-mark.png");
    }

    // Путь к mock изображению
    public Path emptyImagePath(){
        return Paths.get(uploadsPath,"empty.jpg");
    }
}
