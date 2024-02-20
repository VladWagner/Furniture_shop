package gp.wagner.backend.validation.category_request_dto.exceptions;

import gp.wagner.backend.infrastructure.Utils;

import java.nio.charset.StandardCharsets;

public class CategoryDisclosureException extends Exception {

    public CategoryDisclosureException(String message) {

        super(Utils.checkCharset(message, StandardCharsets.UTF_8) ?
                message :
                new String(message.getBytes(StandardCharsets.UTF_8)));
    }
}