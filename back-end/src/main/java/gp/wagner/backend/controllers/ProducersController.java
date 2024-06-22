package gp.wagner.backend.controllers;

import gp.wagner.backend.domain.dto.request.crud.ProducerRequestDto;
import gp.wagner.backend.domain.dto.response.PageDto;
import gp.wagner.backend.domain.dto.response.ProducerRespDto;
import gp.wagner.backend.domain.entities.products.Producer;
import gp.wagner.backend.domain.exceptions.classes.ApiException;
import gp.wagner.backend.infrastructure.Utils;
import gp.wagner.backend.infrastructure.enums.sorting.GeneralSortEnum;
import gp.wagner.backend.infrastructure.enums.sorting.ProducersSortEnum;
import gp.wagner.backend.middleware.Services;
import gp.wagner.backend.validation.producer_request_dto.exceptions.ProducerDisclosureException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping(value = "/api/producers")
public class ProducersController {

    //Выборка всех производителей
    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ProducerRespDto> getAllProducers(@Valid @RequestParam(value = "offset") @Max(100) int pageNum,
                                                    @Valid @RequestParam(value = "limit") @Max(80) int limit,
                                                    @RequestParam(value = "sort_by", defaultValue = "id")  String sortBy,
                                                    @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) {

        Page<Producer> producersPages = Services.producersService.getAll(pageNum, limit,
                ProducersSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(
                producersPages, () -> producersPages.getContent().stream().map(ProducerRespDto::new).toList()
        );
    }

    // Добавление производителя через requestBody, поскольку на фронте объект не всегда будет создаваться через форму
    @PostMapping()
    public ResponseEntity<?> createProducer(@Valid @RequestPart(value = "producer") ProducerRequestDto dto,
                                            @RequestPart(value = "logo") MultipartFile file) throws IOException {

        Producer createdProducer = Services.producersService.create(dto, null);

        if (file != null && !file.isEmpty()) {
            String logoFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            logoFileName = Utils.cleanUrl(Services.fileManageService.saveProducerThumb(logoFileName, file, createdProducer.getId()).toString());


            createdProducer.setProducerLogo(logoFileName);

            Services.producersService.update(createdProducer);
        }

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ProducerRespDto(createdProducer));
    }

    // Изменение производителя
    @PutMapping(value = "/update_producer")
    public ResponseEntity<ProducerRespDto> updateProducer(@Valid @RequestPart(value = "producer") ProducerRequestDto dto,
                                            @RequestPart(value = "logo", required = false) MultipartFile file) throws IOException {

        Producer foundProduct = Services.producersService.getById(dto.getId());

        String logoFileUri = foundProduct.getProducerLogo();

        if (file != null && !file.isEmpty()) {
            String logoFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

            logoFileUri = Utils
                    .cleanUrl(Services.fileManageService.saveProducerThumb(logoFileName, file, foundProduct.getId()).toString());

        }

        Services.producersService.update(dto, logoFileUri);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(new ProducerRespDto(foundProduct));

    }

    /**
     * Скрытие/восстановление из скрытия производителя
     *
     * @param producerId   id производителя, которого необходимо скрыть/восстановить из скрытия
     * @param showState    состояние в которое нужно перевести флаг показа произовдителя и связанных с ним записей
     * @param recoverHeirs флаг восстановления связанных записей (Product, ProductVariants, Baskets и Orders) - параметр необязателен, но если
     *                     showState is true, тогда и recoverHeirs
     */
    @PutMapping(value = "/change_showing_state")
    public ResponseEntity<?> changeShowingStateProducer(@RequestParam(value = "producer_id") long producerId,
                                                        @RequestParam(value = "show_state") boolean showState,
                                                        @RequestParam(value = "recover_heirs", required = false) Boolean recoverHeirs) {

        Producer foundProduct = Services.producersService.getById(producerId);

        // Если товар был скрыт, а сейчас его восстанавливают, но при этом не задали флаг восстановления связанных записей
        if (!foundProduct.getIsShown() && showState && recoverHeirs == null)
            throw new ProducerDisclosureException("Производитель восстанавливается из скрытия, но при этом не задан флаг восстановления связанных записей!");

        // Если задано то же состояние, что было до изменения у производителя
        if (foundProduct.getIsShown() && !showState)
            Services.producersService.hideById(producerId);
        else if (!foundProduct.getIsShown() && showState)
            Services.producersService.recoverHiddenById(producerId, recoverHeirs);
        else
            throw new ApiException("Состояние показа произвоидителя не было изменено!");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(foundProduct.getId());

    }

    // Мягкое удаление производителя
    @DeleteMapping(value = "/delete_by_id/{producer_id}")
    public ResponseEntity<String> deleteProducer(@Valid @PathVariable(value = "producer_id") @Min(0) long producerId) {

        Services.producersService.deleteById(producerId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(String.format("Производитель с id: %d успешно удалён", producerId));


    }// deleteProducer

    // Восстановить удалённого производителя
    @PutMapping(value = "/recover_deleted_by_id")
    public ResponseEntity<String> recoverProducerById(@Valid @RequestParam(value = "producer_id") @Min(0) long producerId,
                                                      @RequestParam(value = "recover_heirs", defaultValue = "true") boolean recoverHeirs) {

        Services.producersService.recoverById(producerId, recoverHeirs);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(String.format("Производитель с id: %d успешно удалён", producerId));


    }// deleteProducer

    // Вывести всех удалённых производителей
    @GetMapping(value = "/all_deleted", produces = MediaType.APPLICATION_JSON_VALUE)
    public PageDto<ProducerRespDto> getAllDeleted(@Valid @RequestParam(value = "offset") @Max(100) int pageNum,
                                                  @Valid @RequestParam(value = "limit") @Max(80) int limit,
                                                  @RequestParam(value = "sort_by", defaultValue = "id")  String sortBy,
                                                  @RequestParam(value = "sort_type", defaultValue = "asc") String sortType) {

        Page<Producer> producersPages = Services.producersService.getAllDeleted(pageNum, limit,
                ProducersSortEnum.getSortType(sortBy), GeneralSortEnum.getSortType(sortType));

        return new PageDto<>(
                producersPages, () -> producersPages.getContent().stream().map(ProducerRespDto::new).toList()
        );
    }
}
