package com.mkhaled.library.common.exception;

/**
 * A request that is valid in shape but breaks a library rule,
 * e.g. borrowing a book with no copies left.
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
