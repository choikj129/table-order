package com.tableorder.exception;

public class StoreNotFoundException extends RuntimeException {
    public StoreNotFoundException(String msg) {
        super(msg);
    }
}
