package com.followup.exception;

import lombok.Getter;

/**
 * 自定义业务异常
 * @author system
 * @since 2026-02-23
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final Integer code;

    public BusinessException(String message) {
        super(message);
        this.code = 500;
    }

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}