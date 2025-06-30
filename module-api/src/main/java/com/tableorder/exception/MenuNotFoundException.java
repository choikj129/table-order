package com.tableorder.exception;

public class MenuNotFoundException extends RuntimeException {
    public MenuNotFoundException(String msg) {
        super(msg);
    }
}
