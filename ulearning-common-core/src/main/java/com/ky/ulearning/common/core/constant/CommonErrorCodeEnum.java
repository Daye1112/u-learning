package com.ky.ulearning.common.core.constant;

import com.ky.ulearning.common.core.exceptions.enums.BaseEnum;
import org.springframework.http.HttpStatus;

/**
 * 服务通用错误码
 *
 * @author luyuhao
 * @since 20/01/29 03:57
 */
public enum CommonErrorCodeEnum implements BaseEnum {
    /**
     * 服务统一错误状态码
     */
    FILE_CANNOT_BE_NULL(HttpStatus.BAD_REQUEST, "文件不能为空!"),
    FILE_TYPE_TAMPER(HttpStatus.BAD_REQUEST, "文件类型篡改!"),
    FILE_TYPE_ERROR(HttpStatus.BAD_REQUEST, "文件类型不规范!"),
    FILE_SIZE_ERROR(HttpStatus.BAD_REQUEST, "文件过大!")
    ;

    private Integer code;
    private String message;

    CommonErrorCodeEnum(HttpStatus httpStatus, String message) {
        this.code = httpStatus.value();
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
