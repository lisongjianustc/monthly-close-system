package com.mc.common.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import com.mc.common.result.ResultCode;

/**
 * 业务异常
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private int code;

    /** 错误信息 */
    private String message;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
        this.message = message;
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }
}
