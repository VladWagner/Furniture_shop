package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.ProducerRequestDto;
import gp.wagner.backend.domain.dto.request.filters.products.ProductFilterDtoContainer;
import gp.wagner.backend.domain.dto.response.BasketRespDto;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.dto.response.ProducerRespDto;
import gp.wagner.backend.domain.dto.response.product.ProductPreviewRespDto;
import gp.wagner.backend.domain.entites.baskets.Basket;
import gp.wagner.backend.domain.entites.products.Producer;
import gp.wagner.backend.domain.entites.products.Product;
import gp.wagner.backend.domain.exception.ApiException;
import gp.wagner.backend.infrastructure.ControllerUtils;
import gp.wagner.backend.infrastructure.SimpleTuple;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.validation.producer_request_dto.exceptions.ProducerDisclosureException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import lombok.SneakyThrows;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping(value = "/api/producers")
public class ProducersController {

    //Выборка всех производителей
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ProducerRespDto> getAllOrders(@Valid @RequestParam(value = "offset") @Max(100) int pageNum,
                                                     @Valid @RequestParam(value = "limit") @Max(80) int limit){

        Page<Producer> producersPages = Services.producersService.getAll(pageNum, limit);

        return new PageDto<>(
                producersPages, () -> producersPages.getContent().stream().map(ProducerRespDto::new).toList()
        );
    }

    // Добавление производителя через requestBody, поскольку на фронте объект не всегда будет создаваться через форму
    @PostMapping()
    public ResponseEntity<Long> createProducer(@Valid @RequestPart(value = "producer") ProducerRequestDto dto,
                                               @RequestPart(value = "logo") MultipartFile file) {

        Producer createdProducer = Services.producersService.create(dto, null);

        if (file != null && !file.isEmpty()) {
            String logoFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            try {
                logoFileName = Utils.cleanUrl(Services.fileManageService.saveProducerOrCategoryThumb(logoFileName, file, null, createdProducer.getId()).toString());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            createdProducer.setProducerLogo(logoFileName);

            Services.producersService.update(createdProducer);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdProducer.getId());
    }

    // Изменение производителя
    @PutMapping(value = "/update_producer")
    public ResponseEntity<Long> updateProducer(@Valid @RequestPart(value = "producer") ProducerRequestDto dto,
                                               @RequestPart(value = "logo", required = false) MultipartFile file) {
        try {

            Producer foundProduct = Services.producersService.getById(dto.getId());

            String logoFileUri = foundProduct.getProducerLogo();

            if (file != null && !file.isEmpty()) {
                String logoFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

                logoFileUri = Utils
                        .cleanUrl(Services.fileManageService.saveProducerOrCategoryThumb(logoFileName, file, null, foundProduct.getId()).toString());

            }

            Services.producersService.update(dto, logoFileUri);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(foundProduct.getId());

        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new ApiException(e.toString());
        }
    }

    /**Скрытие производителя
     * @param producerId id производителя, которого необходимо скрыть/восстановить из скрытия
     * @param showState состояние в которое нужно перевести флаг показа произовдителя и связанных с ним записей
     * @param recoverHeirs флаг восстановления связанных записей (Product, ProductVariants, Baskets и Orders) - параметр необязателен, но если
     *                     showState is true, тогда и recoverHeirs
     * */
    @PutMapping(value = "/change_showing_state")
    public ResponseEntity<Long> changeShowingStateProducer(@RequestParam(value = "producer_id") long producerId,
                                                           @RequestParam(value = "show_state") boolean showState,
                                                           @RequestParam(value = "recover_heirs", required = false) Boolean recoverHeirs) {
        try {

            Producer foundProduct = Services.producersService.getById(producerId);

            // Если товары был скрыт, а сейчас его восстанавливают, но при этом не задали флаг восстановления связанных записей
            if (!foundProduct.getIsShown() && showState && recoverHeirs == null)
               throw new ProducerDisclosureException("Производитель восстанавливается из скрытия, но при этом не задан флаг восстановления связанных записей!");

            // Если задано тоже состояние, что было до изменения у производителя
            if (foundProduct.getIsShown() && !showState)
                Services.producersService.hideById(producerId);
            else if (!foundProduct.getIsShown() && showState)
                Services.producersService.recoverHiddenById(producerId, recoverHeirs);

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(foundProduct.getId());

        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }

    // Мягкое удаление производителя
    @DeleteMapping(value = "/delete_by_id")
    public ResponseEntity<String> deleteProducer(@RequestParam(value = "producer_id") long producerId) {
        try {

            Producer foundProduct = Services.producersService.getById(producerId);

            if (foundProduct != null)
                Services.producersService.deleteById(foundProduct.getId());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(String.format("Производитель с id: %d успешно удалён", producerId));

        } catch (Exception e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
            throw new RuntimeException(e);
        }
    }// deleteProducer

    // Вывести всех удалённых производителей

}
