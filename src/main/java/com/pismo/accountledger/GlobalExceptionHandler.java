package com.pismo.accountledger;

import com.pismo.accountledger.exception.AccountNotFoundException;
import com.pismo.accountledger.exception.DuplicateConstraintException;
import com.pismo.accountledger.exception.DynamoDBTransactionException;
import com.pismo.accountledger.exception.InvalidTransactionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(DuplicateConstraintException.class)
    public ProblemDetail handleDuplicateConstraint(DuplicateConstraintException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        problem.setTitle("Conflict");
        problem.setProperty("field", ex.getField());
        return problem;
    }

    @ExceptionHandler(DynamoDBTransactionException.class)
    public ProblemDetail handleTransactionFailure(DynamoDBTransactionException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        problem.setTitle("DynamoDB Transaction Error");
        return problem;
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleAccountNotFoundException(AccountNotFoundException ex) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setTitle(ex.getMessage());
        problem.setProperty("accountId", ex.getAccountId());
        return problem;
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ProblemDetail handleInvalidTransactionException(InvalidTransactionException ex) {
        log.error("Invalid Transaction, {}", ex.getMessage());
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Unexpected server error.");
        problem.setTitle(ex.getMessage());
        return problem;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericError(Exception ex) {
        log.error("Unknown Exception {}", ex.getMessage());
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected server error.");
        problem.setTitle("Internal Server Error");
        return problem;
    }
}
