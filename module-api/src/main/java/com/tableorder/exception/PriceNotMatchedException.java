package com.tableorder.exception;

public class PriceNotMatchedException extends RuntimeException {
    public PriceNotMatchedException(String msg, int expectPrice, int returnPrice) {
        super(msg + " expect: " + expectPrice + ". returnPrice :" + returnPrice);
    }
}
