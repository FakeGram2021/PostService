package tim6.postservice.adapter.http.advice;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import tim6.postservice.domain.exceptions.AuthorizationException;
import tim6.postservice.domain.exceptions.EntityAlreadyExistsException;
import tim6.postservice.domain.exceptions.EntityNotFoundException;

@ControllerAdvice
public class ErrorHandlingAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    ValidationErrorResponse onMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        BindingResult result = e.getBindingResult();

        ValidationErrorResponse error = new ValidationErrorResponse();
        for (FieldError fieldError : result.getFieldErrors()) {
            error.getViolations()
                    .add(new Violation(fieldError.getField(), fieldError.getDefaultMessage()));
        }

        return error;
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    ValidationErrorResponse onEntityNotFoundException(EntityNotFoundException e) {
        ValidationErrorResponse error = new ValidationErrorResponse();
        error.getViolations().add(new Violation("Cause", e.getMessage()));
        return error;
    }

    @ExceptionHandler(EntityAlreadyExistsException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    ValidationErrorResponse onEntityAlreadyExistsException(EntityAlreadyExistsException e) {
        ValidationErrorResponse error = new ValidationErrorResponse();
        error.getViolations().add(new Violation("Cause", e.getMessage()));
        return error;
    }

    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    ValidationErrorResponse onAuthorizationException(AuthorizationException e) {
        ValidationErrorResponse error = new ValidationErrorResponse();
        error.getViolations().add(new Violation("Cause", e.getMessage()));
        return error;
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    ValidationErrorResponse onEmptyResultDataAccessException(EmptyResultDataAccessException e) {
        ValidationErrorResponse error = new ValidationErrorResponse();
        error.getViolations().add(new Violation("Cause", e.getMessage()));
        return error;
    }
}
