package com.optimagrowth.license.controller;

import com.optimagrowth.license.model.util.ErrorMessage;
import com.optimagrowth.license.model.util.ResponseWrapper;
import com.optimagrowth.license.model.util.RestErrorList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

import static java.util.Collections.singletonMap;

@ControllerAdvice
@EnableWebMvc
public class ExceptionController extends ResponseEntityExceptionHandler {

    /**
     * handleException - Handles all the Exception receiving a request, responseWrapper.
     *
     * @param request incoming request
     * @param responseWrapper to wrap the response with
     * @return ResponseEntity<ResponseWrapper>
     */
    @ExceptionHandler(value = {Exception.class})
    public @ResponseBody
    ResponseEntity<ResponseWrapper> handleException(HttpServletRequest request,
                                                    ResponseWrapper responseWrapper) {

        return ResponseEntity.ok(responseWrapper);
    }

    /**
     * handleIOException - Handles all the Authentication Exceptions of the application.
     *
     * @param request incoming request
     * @param e runtimeException
     * @return ResponseEntity<ResponseWrapper>
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseWrapper> handleIOException(HttpServletRequest request, RuntimeException e) {

        RestErrorList errorList = new RestErrorList(HttpStatus.NOT_ACCEPTABLE, new ErrorMessage(e.getMessage(), e.getMessage()));
        ResponseWrapper responseWrapper = new ResponseWrapper(null, singletonMap("status", HttpStatus.NOT_ACCEPTABLE), errorList);

        return ResponseEntity.ok(responseWrapper);
    }
}
