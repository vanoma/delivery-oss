package com.vanoma.api.order.web;


import com.vanoma.api.order.utils.LanguageUtils;
import com.vanoma.api.utils.error.ErrorResponse;
import com.vanoma.api.utils.error.ErrorResponseType;
import com.vanoma.api.utils.exceptions.ExpectedServerError;
import com.vanoma.api.utils.exceptions.InvalidParameterException;
import com.vanoma.api.utils.exceptions.ResourceNotFoundException;
import com.vanoma.api.utils.exceptions.UnauthorizedAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.persistence.EntityNotFoundException;

@RestControllerAdvice
public class RestExceptionHandler {
    Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);

    @Autowired
    private LanguageUtils languageUtils;

    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse requestHandlingNoHandlerFound(Exception ex) {
        return new ErrorResponse(
                ErrorResponseType.RESOURCE_NOT_FOUND.name(),
                languageUtils.getLocalizedMessage("global.resourceNotFound")
        );
    }

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse requestHandlingEntityNotFound(Exception ex) {
        return new ErrorResponse(
                ErrorResponseType.RESOURCE_NOT_FOUND.name(),
                ex.getMessage()
        );
    }

    // Custom Not Found response for entities, as opposed to unknown endpoints.
    // E.g. GET /api/unknown-endpoint will trigger NoHandlerFoundException
    // while GET /api/addresses/<unknown-address-id> will trigger ResourceNotFound
    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(value = HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse requestHandlingResourceNotFound(Exception ex) {
        return new ErrorResponse(
                ErrorResponseType.RESOURCE_NOT_FOUND.name(),
                languageUtils.getLocalizedMessage(ex.getMessage())
        );
    }

    @ExceptionHandler(InvalidParameterException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse requestHandlingInvalidParameter(Exception ex) {
        return new ErrorResponse(
                ErrorResponseType.INVALID_REQUEST.name(),
                languageUtils.getLocalizedMessage(ex.getMessage())
        );
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    @ResponseStatus(value = HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorResponse requestHandlingUnauthorizedAccess(Exception ex) {
        return new ErrorResponse(
                ErrorResponseType.AUTHORIZATION_ERROR.name(),
                languageUtils.getLocalizedMessage(ex.getMessage())
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleRequestBodyParsingError(Exception ex) {
        return new ErrorResponse(
                ErrorResponseType.INVALID_REQUEST.name(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(MissingRequestValueException.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleMissingRequestValueError(Exception ex) {
        return new ErrorResponse(
                ErrorResponseType.INVALID_REQUEST.name(),
                ex.getMessage()
        );
    }

    @ExceptionHandler(ExpectedServerError.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse requestHandlingExpectedServerError(Exception ex) {
        return new ErrorResponse(
                ErrorResponseType.INTERNAL_ERROR.name(),
                languageUtils.getLocalizedMessage(ex.getMessage())
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse requestHandlingServerError(Exception ex) {
        logger.error(ex.getMessage(), ex);
        return new ErrorResponse(
                ErrorResponseType.INTERNAL_ERROR.name(),
                ex.getMessage()
        );
    }
}
