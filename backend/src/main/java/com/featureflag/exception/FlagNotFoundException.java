package com.featureflag.exception;

import java.util.UUID;

public class FlagNotFoundException extends RuntimeException {

    public FlagNotFoundException(UUID id) {
        super("Flag not found: " + id);
    }

    public FlagNotFoundException(String name) {
        super("Flag not found: " + name);
    }
}
