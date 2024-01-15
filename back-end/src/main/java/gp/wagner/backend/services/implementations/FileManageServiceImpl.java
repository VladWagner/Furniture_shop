package gp.wagner.backend.services.implementations;

import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.infrastructure.Constants;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.services.interfaces.FileManageService;
import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.geometry.Positions;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

//Сервис для работы с файлами - загрузка изображений товаров и формирование thumbnails
@Service
public class FileManageServiceImpl implements FileManageService {

    //Загрузка файла изображения товара в папку
    @Override
    public Resource saveFile(String fileName, MultipartFile multipartFile, Long categoryId, Long productId, Long productVariantId) throws IOException {

        //Стандартный путь
        //Path newPath = Constants.UPLOAD_PATH_IMG;

        StringBuilder sb = new StringBuilder(Constants.UPLOAD_PATH_IMG.toString());
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
                .watermark(Positions.CENTER_RIGHT, ImageIO.read(Constants.WATERMARK_PATH.toFile()), 0.1f) //0.3 - водяной знак будет занимать 30% картинки
                .toFile(filePath.toFile());

        return new UrlResource(filePath.toUri());
    }

    @Override
    public Resource saveFile(String fileName, MultipartFile multipartFile, Long categoryId, Long productId) throws IOException {
        return saveFile(fileName, multipartFile, categoryId, productId, null);
    }

    @Override
    public Resource saveProducerOrCategoryThumb(String fileName, MultipartFile multipartFile, Long categoryId, Long producerId) throws IOException {

        if (categoryId != null && producerId != null)
            throw new ApiException("Невозможно сохранить файл для производителя и категории одновременно!");

        StringBuilder sb = new StringBuilder(Constants.UPLOAD_PATH_UTIL.toString());

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
        String newFileName = categoryId != null ? "category" :
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

    //Загрузка изображения для предосмотра товара
    @Override
    public Resource saveThumbnail(String fileUri, Long categoryId, Long productId) throws IOException {

        File file = ResourceUtils.getFile(fileUri);

        //Сформировать путь для записи файла
        Path newPath = Constants.UPLOAD_PATH_THUMB;

        //Если задана категория, тогда добавить название директивы с категорией
        if (categoryId != null)
            newPath = Paths.get(Constants.UPLOAD_PATH_THUMB.toString(), String.format("/%s", categoryId));

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
    public void deleteFile(URI filePath) throws IOException {
        Path path = Paths.get(filePath);

        if (!Files.exists(path))
            return;
            //throw new ApiException(String.format("Файла %s не существует!", path.getFileName()));

        Files.delete(path);

    }

    @Override
    public boolean isExists(URI filePath) {

        boolean result = false;
        try {
            Path path = Paths.get(filePath);
            result = Files.exists(path);
        } catch (Exception e) {
            return false;
        }

        return result;
    }

}
