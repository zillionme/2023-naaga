package com.now.naaga.common.exception;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ControllerExceptionHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ExceptionResponse> handleBaseException(final BaseException e) {
        final BaseExceptionType baseExceptionType = e.exceptionType();
        final ExceptionResponse exceptionResponse = new ExceptionResponse(baseExceptionType.errorCode(), baseExceptionType.errorMessage());
        log.warn("error = {}", exceptionResponse, e);
        return ResponseEntity.status(baseExceptionType.httpStatus()).body(exceptionResponse);
    }

    @ExceptionHandler({
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            HttpMediaTypeNotSupportedException.class
    })
    public ResponseEntity<ExceptionResponse> handleTypeMismatchException(final Exception e) {
        final CommonExceptionType commonExceptionType = CommonExceptionType.INVALID_REQUEST_BODY;
        final ExceptionResponse exceptionResponse = new ExceptionResponse(commonExceptionType.errorCode(), commonExceptionType.errorMessage());
        log.warn("error = {}", exceptionResponse, e);
        return ResponseEntity.status(commonExceptionType.httpStatus()).body(exceptionResponse);
    }

    @ExceptionHandler(InternalException.class)
    public ResponseEntity<ExceptionResponse> handleInternalException(final InternalException e){
        final BaseExceptionType internalExceptionType = e.exceptionType();

//        log.error("errorCode = {} \n message = {}",
//                internalExceptionType.errorCode(),
//                internalExceptionType.errorMessage());

        log.error("errorCode = {} \n message = {} \n error = {}",
                internalExceptionType.errorCode(),
                internalExceptionType.errorMessage(),
                 e.getMessage() , e);

        final ExceptionResponse exceptionResponse = new ExceptionResponse(10000, "예기치 못한 오류입니다");
        return ResponseEntity.status(internalExceptionType.httpStatus())
                .body(exceptionResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleException(final Exception e){
        log.error("error = {}"+ e.getMessage() , e);

        final ExceptionResponse exceptionResponse = new ExceptionResponse(10000, "예기치 못한 오류입니다");
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(exceptionResponse);
    }
}
