package gp.wagner.backend.services.implementations;

import gp.wagner.backend.configurations.FileUploadProperties;
import gp.wagner.backend.domain.entities.reviews.Review;
import gp.wagner.backend.domain.entities.users.User;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.Constants;
import gp.wagner.backend.infrastructure.ProfileImageGenerator;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.services.interfaces.FileManageService;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URI;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

//Сервис для работы с файлами - загрузка изображений товаров и формирование thumbnails
@Service
public class FileManageServiceImpl implements FileManageService {

    private FileUploadProperties filesPaths;

    @Autowired
    public void setFilesPaths(FileUploadProperties filesPaths) {

        this.filesPaths = filesPaths;

        if (filesPaths.getUploadsPath() == null || filesPaths.getUploadsPath().isBlank())
            this.filesPaths.setUploadsPath(Constants.UPLOAD_URI);

    }

    @Override
    public FileUploadProperties getFilesPaths() {
        return filesPaths;
    }

    //Загрузка файла изображения товара в папку
    @Override
    public Resource savePvImgFile(String fileName, MultipartFile multipartFile, Long categoryId, Long productId, Long productVariantId) throws IOException {

        //Стандартный путь
        //Path newPath = Constants.UPLOAD_PATH_IMG;

        //StringBuilder sb = new StringBuilder(Constants.UPLOAD_PATH_IMG.toString());
        StringBuilder sb = new StringBuilder(filesPaths.uploadImgPath().toString());

        //Если задана категория, тогда добавить её в путь файла
        if (categoryId != null)
            sb.append(String.format("/%d", categoryId));
            //newPath = Paths.get(Constants.UPLOAD_PATH_IMG.toString(), String.format("/%d", categoryId));

        //Если задан id товар, тогда тоже добавить его в путь файла
        if (productId != null)
            sb.append(String.format("/%d", productId));
            //newPath = Paths.get(newPath.toString(),String.format("/%d", productId));

        //Если задан id варианта товара, тогда тоже добавить его в путь файла
        if (productVariantId != null)
            sb.append(String.format("/%d", productVariantId));
            //newPath = Paths.get(newPath.toString(),String.format("/%d", productVariantId));

        Path newPath = Paths.get(sb.toString());

        if (!Files.exists(newPath))
            Files.createDirectories(newPath);

        //Сформировать код для уникализации имени файла
        String strCode = RandomStringUtils.random(8, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ123456789");
        String extension = fileName.substring(fileName.lastIndexOf("."));
        fileName = fileName.replace(extension, "");

        //Добавить к пути имя сгенерированное файла
        Path filePath = newPath.resolve(String.format("%s-%s%s", fileName, strCode, extension));

        //Записать файл
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath.toFile()))) {
            bos.write(multipartFile.getBytes());
        }

        //Задать водяной знак
        Thumbnails.of(filePath.toFile())
                .scale(1)
                .watermark(Positions.CENTER_RIGHT, ImageIO.read(/*Constants.WATERMARK_PATH.toFile()*/ filesPaths.watermarkPath().toFile()), 0.1f) //0.3 - водяной знак будет занимать 30% картинки
                .toFile(filePath.toFile());

        return new UrlResource(filePath.toUri());
    }

    @Override
    public Resource saveProductImgFile(String fileName, MultipartFile multipartFile, Long categoryId, Long productId) throws IOException {
        return savePvImgFile(fileName, multipartFile, categoryId, productId, null);
    }

    public Resource saveProducerOrCategoryThumb(String fileName, MultipartFile multipartFile, Long producerId, Long categoryId, boolean isRepeatingCategory) throws IOException {

        if (categoryId != null && producerId != null)
            throw new ApiException("Невозможно сохранить файл для производителя и категории одновременно!");

        //StringBuilder sb = new StringBuilder(Constants.UPLOAD_PATH_UTIL.toString());
        StringBuilder sb = new StringBuilder(filesPaths.uploadUtilsPath().toString());

        //Если задана категория, тогда добавить ещё один каталог
        if (categoryId != null)
            sb.append("/categories_previews");

        //Если задан id производителя
        if (producerId != null)
            sb.append("/producers_logos");

        Path newPath = Paths.get(sb.toString());

        if (!Files.exists(newPath))
            Files.createDirectories(newPath);

        String extension = fileName.substring(fileName.lastIndexOf("."));

        // Имя файла без расширения
        fileName = fileName.substring(0, fileName.lastIndexOf("."));

        // Сформировать описание сохраняемого файла (для категории/производителя)
        String newFileName = categoryId != null ? (!isRepeatingCategory ? "category_m" : "category_r") :
                producerId != null ? "producer" : "some_file";

        // Добавить уникальный идентификатор
        long id = categoryId != null ? categoryId : producerId != null ? producerId : Utils.getRandom(1000, 1_000_000);

        // Добавить к пути имя сгенерированное файла -
        // что сохраняется (производитель/категория)_id производителя/категории_первые несколько символов имени исходного файла_расширение
        Path filePath = newPath.resolve(String.format("%s-%d-%s%s", newFileName, id,
                fileName.length() >= 2 ? fileName.substring(0,2) : fileName.charAt(0), extension));

        // Записать файл
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath.toFile()))) {
            bos.write(multipartFile.getBytes());
        }

        return new UrlResource(filePath.toUri());
    }

    @Override
    public Resource saveProducerThumb(String fileName, MultipartFile multipartFile, Long producerId) throws IOException {
        return saveProducerOrCategoryThumb(fileName, multipartFile, producerId, null, false);
    }

    @Override
    public Resource saveCategoryThumb(String fileName, MultipartFile multipartFile, Long categoryId, boolean isRepeating) throws IOException {
        return saveProducerOrCategoryThumb(fileName, multipartFile, null, categoryId, isRepeating);
    }

    //Загрузка изображения для предосмотра товара
    @Override
    public Resource saveThumbnail(String fileUri, Long categoryId, Long productId) throws IOException {

        File file = ResourceUtils.getFile(fileUri);

        //Сформировать путь для записи файла
        //Path newPath = Constants.UPLOAD_PATH_THUMB;
        Path newPath = filesPaths.uploadThumbsPath();

        //Если задана категория, тогда добавить название директивы с категорией
        if (categoryId != null)
            newPath = Paths.get(filesPaths.uploadThumbsPath().toString(), String.format("/%s", categoryId));
        //newPath = Paths.get(Constants.UPLOAD_PATH_THUMB.toString(), String.format("/%s", categoryId));

        //Если задан id товара, тогда добавить директиву с id товара
        if (productId != null)
            newPath = Paths.get(newPath.toString(),String.format("/%d", productId));

        if (!Files.exists(newPath))
            Files.createDirectories(newPath);

        //Сформировать файл со сжатым изображением

        String fileName = file.getName();
        String extension = fileName.substring(fileName.lastIndexOf('.'));

        //Сформировать имя файла без расширения
        fileName = fileName.replace(extension, "");

        //Сформировать полное имя файла с приставкой '-thumb'
        File newFile = new File(String.format("%s/%s%s%s", newPath.toAbsolutePath(), fileName, Constants.THUMB_SUFFICE, extension));

        //Сформировать новое изображение в уменьшенном масштабе
        Thumbnails.of(file)
                .scale(0.33)
                .outputQuality(1)
                .outputFormat(extension.replace('.',' ').trim())
                .toFile(newFile);


        return new UrlResource(Paths.get(newFile.getPath()).toUri());
    }

    @Override
    public Resource saveUserProfileImg(String fileName, MultipartFile multipartFile, long userId) throws IOException {
        if (userId <= 0)
            throw new ApiException("Невозможно сохранить файл для несуществующего пользователя!");

        //String stringPath = String.format("%s/user_%d",Constants.UPLOAD_PATH_USERS.toString(), userId);
        String stringPath = String.format("%s/user_%d",filesPaths.uploadUsersPhotosPath(), userId);

        Path newPath = Paths.get(stringPath);

        if (!Files.exists(newPath))
            Files.createDirectories(newPath);

        String extension = fileName.substring(fileName.lastIndexOf("."));

        // Имя файла без расширения
        fileName = fileName.substring(0, fileName.lastIndexOf("."));

        // Добавить случайный идентификатор
        long fileId = new Date().getTime()/*Utils.getRandom(1000, 1_000_000)*/;

        // Добавить к пути имя сгенерированное файла
        Path filePath = newPath.resolve(String.format("%d-%s%s",fileId,
                fileName.length() >= 2 ? fileName.substring(0,2) : fileName.charAt(0), extension));

        // Записать файл
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath.toFile()))) {
            bos.write(multipartFile.getBytes());
        }

        return new UrlResource(filePath.toUri());
    }

    // Загрузить изображение в отзыве
    @Override
    public Resource saveReviewImg(String fileName, MultipartFile multipartFile, Review review) throws IOException {
        if (review == null)
            throw new ApiException("Невозможно сохранить файл для несуществующего отзыва!");

        //StringBuilder sb = new StringBuilder(Constants.UPLOAD_PATH_REVIEWS.toString());
        StringBuilder sb = new StringBuilder(filesPaths.uploadReviewsPath().toString());

        // Если в товаре задана категория, тогда добавить её в путь файла
        if (review.getProduct() != null && review.getProduct().getCategory() != null)
            sb.append(String.format("/category_%d", review.getProduct().getCategory().getId()));

        // Если задан id товар, тогда тоже добавить его в путь файла
        if (review.getProduct() != null)
            sb.append(String.format("/%d", review.getProduct().getId()));

        Path newPath = Paths.get(sb.toString());

        if (!Files.exists(newPath))
            Files.createDirectories(newPath);

        String extension = fileName.substring(fileName.lastIndexOf("."));

        // Имя файла без расширения
        fileName = fileName.substring(0, fileName.lastIndexOf("."));

        // Добавить случайный идентификатор
        long fileId = new Date().getTime();

        // Добавить к пути имя сгенерированное файла
        Path filePath = newPath.resolve(String.format("%d-%s%s",fileId,
                fileName.length() >= 2 ? fileName.substring(0,2) : fileName.charAt(0), extension));


        // Записать файл
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath.toFile()))) {
            bos.write(multipartFile.getBytes());
        }

        return new UrlResource(filePath.toUri());
    }

    @Override
    public Resource generateAndSaveUserImg(User user) throws IOException {

        if (user == null ||
            ((user.getName() == null || user.getName().isBlank()) &&
                    (user.getUserLogin() == null || user.getUserLogin().isBlank())) || user.getId() == null)
            throw new ApiException("Пользователь для генерации и сохранения файла задан некорректно!");

        String symbols;

        // Получить первые символы имени или логина пользователя
        if (user.getName() != null && !user.getName().isBlank())
            symbols = Utils.getFistSymbols(user.getName());
        else
            symbols = Utils.getFistSymbols(user.getUserLogin()).toUpperCase();

        // Сформировать изображение исходя из 1-х строк полей User

        BufferedImage createdImg = ProfileImageGenerator.createImgWithChars(symbols);

        //String stringPath = String.format("%s/user_%d",Constants.UPLOAD_PATH_USERS.toString(), user.getId());
        String stringPath = String.format("%s/user_%d",filesPaths.uploadUsersPhotosPath(), user.getId());

        Path path = Paths.get(stringPath);

        if (!Files.exists(path))
            Files.createDirectories(path);


        String fileName = String.format("%d-%s-%d.jpeg", new Date().getTime(), Constants.GENERATED_USER_IMG_CODE, user.getId());

        path = path.resolve(fileName);

        ImageIO.write(createdImg, "jpeg", path.toFile());

        return new UrlResource(path.toUri());
    }

    @Override
    public void deleteFile(URI filePath) throws IOException {

        if (filePath == null)
            return;

        Path path = Paths.get(filePath);

        if (!Files.exists(path))
            return;
            //throw new ApiException(String.format("Файла %s не существует!", path.getFileName()));

        Files.delete(path);

    }
    @Override
    public String renameFile(String filePath, String newName) {

        if (filePath == null)
            return null;

        File file = new File(filePath);

        String cleanPath = filePath.substring(filePath.lastIndexOf('/'));
        String extension = filePath.substring(filePath.lastIndexOf('.'));

        if (cleanPath.isBlank() || extension.isBlank())
            return null;

        if (newName.indexOf('/') == 0)
            newName = newName.substring(0, 1);

        String newFilePath = String.format("%s/%s.%s", cleanPath, newName, extension);

        return file.renameTo(new File(newFilePath)) ? newFilePath : null;
    }

    @Override
    public boolean isExists(URI filePath) {

        boolean result;
        try {
            Path path = Paths.get(filePath);
            result = Files.exists(path);
        } catch (Exception e) {
            return false;
        }

        return result;
    }

}
