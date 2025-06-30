package com.tableorder.exception;

import com.tableorder.dto.ResponseDTO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ResponseDTO> handleNotFound(NoHandlerFoundException ex) {
        log.error(ex.getMessage());
        ResponseDTO response = new ResponseDTO(404, "Requested resource not found.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler({StoreNotFoundException.class, MenuNotFoundException.class})
    public ResponseEntity<ResponseDTO> handleEntityNotFound(RuntimeException ex) {
        log.error(ex.getMessage());
        ResponseDTO responseData = new ResponseDTO(404, ex.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseData);
    }
    @ExceptionHandler({PriceNotMatchedException.class})
    public ResponseEntity<ResponseDTO> handleEntityNotMatched(RuntimeException ex) {
        log.error(ex.getMessage());
        ResponseDTO responseData = new ResponseDTO(409, ex.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseData);
    }

    @ExceptionHandler({RuntimeException.class})
    public ResponseEntity<ResponseDTO> handleRuntimeException(RuntimeException ex) {
        String errorMessage = ex.getMessage();

        log.error(errorMessage);
        ResponseDTO responseData = new ResponseDTO(500, errorMessage);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseData);
    }
}
