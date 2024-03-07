package gp.wagner.backend.domain.exceptions.validation_errors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ValidationExceptionDto {

    // Список полей DTO, где были обнаружены ошибки
    private List<Violation> violations;

}
