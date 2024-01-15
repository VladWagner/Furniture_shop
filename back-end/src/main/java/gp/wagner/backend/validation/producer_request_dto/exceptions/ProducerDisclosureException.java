package gp.wagner.backend.validation.producer_request_dto.exceptions;

import gp.wagner.backend.infrastructure.Utils;

import java.nio.charset.StandardCharsets;

public class ProducerDisclosureException extends RuntimeException {

    public ProducerDisclosureException(String message) {

        super(Utils.checkCharset(message, StandardCharsets.UTF_8) ?
                message :
                new String(message.getBytes(StandardCharsets.UTF_8)));
    }
}