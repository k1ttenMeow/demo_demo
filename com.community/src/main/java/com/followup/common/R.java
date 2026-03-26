package com.followup.common;

import lombok.Data;
import java.io.Serializable;

/**
 * 通用返回结果
 *
 * @author followup
 * @date 2026-03-26
 */
@Data
public class R<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 成功状态码
     */
    public static final int SUCCESS = 200;

    /**
     * 失败状态码
     */
    public static final int ERROR = 500;

    /**
     * 响应码
     */
    private int code;

    /**
     * 响应消息
     */
    private String msg;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 成功返回结果
     */
    public static <T> R<T> success() {
        R<T> r = new R<>();
        r.setCode(SUCCESS);
        r.setMsg("操作成功");
        return r;
    }

    /**
     * 成功返回结果（带数据）
     */
    public static <T> R<T> success(T data) {
        R<T> r = new R<>();
        r.setCode(SUCCESS);
        r.setMsg("操作成功");
        r.setData(data);
        return r;
    }

    /**
     * 失败返回结果
     */
    public static <T> R<T> error(String msg) {
        R<T> r = new R<>();
        r.setCode(ERROR);
        r.setMsg(msg);
        return r;
    }
}