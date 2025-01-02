package com.anil.BookApp.exceptions;

public class OperationNotPermittedException extends RuntimeException{

    public OperationNotPermittedException(String message){
        super(message);
    }
}
