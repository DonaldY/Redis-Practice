package com.donaldy.redispractice.config.handler;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AsyncException extends Exception {
    private int code;
    private String errorMessage;
}
