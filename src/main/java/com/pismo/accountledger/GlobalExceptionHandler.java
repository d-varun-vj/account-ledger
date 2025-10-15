package com.pismo.accountledger;

import com.pismo.accountledger.exception.AccountNotFoundException;
import com.pismo.accountledger.exception.DuplicateConstraintException;
import com.pismo.accountledger.exception.DynamoDBTransactionException;
import com.pismo.accountledger.exception.InvalidTransactionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateConstraintException.class)
    public ProblemDetail handleDuplicateConstraint(DuplicateConstraintException ex) {
        log.error("DuplicateConstraint, {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflict");
        problem.setProperty("field", ex.getField());
        return problem;
    }

    @ExceptionHandler(DynamoDBTransactionException.class)
    public ProblemDetail handleTransactionFailure(DynamoDBTransactionException ex) {
        log.error("TransactionFailure, {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problem.setTitle("DynamoDB Transaction Error");
        return problem;
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFoundException(AccountNotFoundException ex) {
        log.error("AccountNotFoundException, {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(ex.getMessage());
        problem.setProperty("accountId", ex.getAccountId());
        return problem;
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ProblemDetail handleInvalidTransactionException(InvalidTransactionException ex) {
        log.error("Invalid Transaction, {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        problem.setTitle(ex.getMessage());
        return problem;
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<List<ProblemDetail>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<ProblemDetail> problemDetails = ex.getBindingResult().getFieldErrors().stream()
                .map((error) -> ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, error.getDefaultMessage()))
                .toList();
        log.error("Request validation error: ", ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetails);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ProblemDetail> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        log.error("MethodArgumentTypeMismatchException, {}", ex.getMessage());
        String errorMessage = String.format("Invalid value '%s' for parameter '%s'. Expected type: '%s'.", ex.getValue(), ex.getName(), ex.getRequiredType().getSimpleName());
        ProblemDetail response = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler({MissingRequestHeaderException.class})
    public ResponseEntity<ProblemDetail> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        log.error("MissingRequestHeaderException, {}", ex.getHeaderName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getBody());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericError(Exception ex) {
        log.error("Unknown Exception {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error.");
        problem.setTitle("Internal Server Error");
        return problem;
    }
}
