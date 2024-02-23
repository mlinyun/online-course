package com.mlinyun.onlinecourse.basics.utils;

import com.mlinyun.onlinecourse.basics.baseVo.Result;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 统一结果返回工具类
 * @param <T>
 */
public class ResultUtil<T> {

    private Result<T> result;

    private static final String DEFAULT_SUCCESS_STR = "OK";

    private static final String DEFAULT_FAIL_STR = "操作失败";

    public ResultUtil() {
        result = new Result<>();
        result.setSuccess(true);
        result.setMessage(DEFAULT_SUCCESS_STR);
        result.setCode(200);
    }

    @Schema(description = "成功返回数据")
    public Result<T> setData(T t) {
        this.result.setResult(t);
        this.result.setCode(200);
        return this.result;
    }

    @Schema(description = "成功返回数据和备注")
    public Result<T> setDataAndMessage(T t, String msg) {
        this.result.setResult(t);
        this.result.setCode(200);
        this.result.setMessage(msg);
        return this.result;
    }

    @Schema(description = "成功返回备注")
    public Result<T> setSuccessMsg(String msg) {
        this.result.setSuccess(true);
        this.result.setMessage(msg);
        this.result.setCode(200);
        this.result.setResult(null);
        return this.result;
    }

    @Schema(description = "成功返回数据和备注")
    public Result<T> setData(T t, String msg) {
        this.result.setResult(t);
        this.result.setCode(200);
        this.result.setMessage(msg);
        return this.result;
    }

    @Schema(description = "错误返回备注")
    public Result<T> setErrorMsg(String msg) {
        this.result.setSuccess(false);
        this.result.setMessage(msg);
        this.result.setCode(500);
        return this.result;
    }

    @Schema(description = "错误返回状态码备注")
    public Result<T> setErrorMsg(Integer code, String msg) {
        this.result.setSuccess(false);
        this.result.setMessage(msg);
        this.result.setCode(code);
        return this.result;
    }

    public static <T> Result<T> data(T t) {
        return new ResultUtil<T>().setData(t);
    }

    public static <T> Result<T> data() {
        return new ResultUtil<T>().setData(null);
    }

    public static <T> Result<T> data(T t, String msg) {
        return new ResultUtil<T>().setData(t, msg);
    }

    public static <T> Result<T> error(String msg) {
        return new ResultUtil<T>().setErrorMsg(msg);
    }

    public static <T> Result<T> error() {
        return new ResultUtil<T>().setErrorMsg(DEFAULT_FAIL_STR);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new ResultUtil<T>().setErrorMsg(code, msg);
    }

    public static <T> Result<T> success(String msg) {
        return new ResultUtil<T>().setSuccessMsg(msg);
    }

    public static <T> Result<T> success() {
        return new ResultUtil<T>().setSuccessMsg(DEFAULT_SUCCESS_STR);
    }
}
