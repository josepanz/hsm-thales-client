package com.hsmclient.api;

import com.hsmclient.api.dto.ApiErrorResponse;
import com.hsmclient.domain.exception.HsmException;
import com.hsmclient.domain.exception.HsmTransportException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Nunca se loguea el payload de la request (contiene llaves cifradas y PIN blocks):
 * solo el codigo de error Thales, que es informacion no sensible pensada para logging.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(HsmException.class)
    public ResponseEntity<ApiErrorResponse> handleHsmException(HsmException e) {
        log.warn("HSM devolvio error: {}", e.errorCode());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiErrorResponse(e.rawCode(), e.getMessage()));
    }

    @ExceptionHandler(HsmTransportException.class)
    public ResponseEntity<ApiErrorResponse> handleTransportException(HsmTransportException e) {
        log.error("Fallo de transporte hablando con el HSM: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiErrorResponse("TRANSPORT_ERROR", "El HSM no esta disponible"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ApiErrorResponse("INVALID_INPUT", e.getMessage()));
    }
}
