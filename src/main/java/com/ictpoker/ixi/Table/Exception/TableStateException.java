package com.ictpoker.ixi.Table.Exception;

public class TableStateException extends Exception {

    public TableStateException(final String message) {

        super(message);
    }

    public TableStateException(final String message, Exception e) {

        super(message, e);
    }
}
