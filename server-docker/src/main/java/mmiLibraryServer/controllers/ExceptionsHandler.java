/*
 * Copyright (C) 2022 IUT Laval - Le Mans Université.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package mmiLibraryServer.controllers;

import java.time.ZonedDateTime;
import java.util.NoSuchElementException;
import javax.servlet.http.HttpServletRequest;
import mmiLibraryServer.services.exceptions.HasOngoingLoanException;
import mmiLibraryServer.services.exceptions.LoanImpossibleException;
import mmiLibraryServer.services.exceptions.MemberWithUnreturnedLoanException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author Rémi Venant
 */
@ControllerAdvice
public class ExceptionsHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleResourceNotFound(HttpServletRequest request, NoSuchElementException ex) {
        final HttpStatus status = HttpStatus.NOT_FOUND;
        final String error = "Resource not found";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleBadArgument(HttpServletRequest request, IllegalArgumentException ex) {
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        final String error = "Invalid argument";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(LoanImpossibleException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleLoanImpossible(HttpServletRequest request, LoanImpossibleException ex) {
        final HttpStatus status = HttpStatus.NOT_ACCEPTABLE;
        final String error = "Not acceptable loan";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(MemberWithUnreturnedLoanException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleMemberWithUnreturnedLoan(HttpServletRequest request, MemberWithUnreturnedLoanException ex) {
        final HttpStatus status = HttpStatus.NOT_ACCEPTABLE;
        final String error = "Member with still unreturned loan";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleMessageNotReadable(HttpServletRequest request, HttpMessageNotReadableException ex) {
        final HttpStatus status = HttpStatus.BAD_REQUEST;
        final String error = "Invalid argument";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleMemdiaTypeNotSupported(HttpServletRequest request, HttpMediaTypeNotSupportedException ex) {
        final HttpStatus status = HttpStatus.UNSUPPORTED_MEDIA_TYPE;
        final String error = "Invalid argument";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(DuplicateKeyException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleMemberWithUnreturnedLoan(HttpServletRequest request, DuplicateKeyException ex) {
        final HttpStatus status = HttpStatus.CONFLICT;
        final String error = "An unique value is already present.";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(HasOngoingLoanException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleHasOngoingLoan(HttpServletRequest request, HasOngoingLoanException ex) {
        final HttpStatus status = HttpStatus.CONFLICT;
        final String error = "On going loan";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleMethodUnsupported(HttpServletRequest request, HttpRequestMethodNotSupportedException ex) {
        final HttpStatus status = HttpStatus.METHOD_NOT_ALLOWED;
        final String error = "Method unsupported.";
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    @ExceptionHandler(Throwable.class)
    public @ResponseBody
    ResponseEntity<ErrorMessage> handleMemberWithUnreturnedLoan(HttpServletRequest request, Throwable ex) {
        final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        final String error = "Unmanaged exception: " + ex.getClass().getCanonicalName();
        return new ResponseEntity<>(createErrorMessage(status, error, ex.getMessage(), request), status);
    }

    private static ErrorMessage createErrorMessage(HttpStatus status, String error, String message, HttpServletRequest request) {
        return new ErrorMessage(ZonedDateTime.now(), status.value(), error, message, request.getServletPath());
    }

    public static class ErrorMessage {

        private ZonedDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private String path;

        public ErrorMessage() {
        }

        public ErrorMessage(ZonedDateTime timestamp, int status, String error, String message, String path) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.path = path;
        }

        public ZonedDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(ZonedDateTime timestamp) {
            this.timestamp = timestamp;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

    }
}
