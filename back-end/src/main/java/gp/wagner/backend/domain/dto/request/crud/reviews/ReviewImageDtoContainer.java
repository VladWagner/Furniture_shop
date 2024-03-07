package gp.wagner.backend.domain.dto.request.crud.reviews;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;

import java.util.List;

// DTO для редактирования изображений в отзыве
public record ReviewImageDtoContainer(@Nullable @JsonProperty("review_image_dto_list") List<ReviewImageDto> reviewImageDtoList,
                                      @Nullable @JsonProperty("deleted_images_id") List<Long> deletedImagesId){}
