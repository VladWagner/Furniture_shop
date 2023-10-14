package gp.wagner.backend.domain.dto.request.crud.product;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotNull;

import java.util.List;

//DTO для редактирования варианта товара - заданные + удалённые изображения
public record ProductImageDtoContainer (@NotNull List<ProductImageDto> productImageDtoList,
                                        @Nullable List<Long> deletedImagesId){}
