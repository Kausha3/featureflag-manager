package com.featureflag.exception;

public class DuplicateRuleException extends RuntimeException {

    public DuplicateRuleException(String message) {
        super(message);
    }
}
