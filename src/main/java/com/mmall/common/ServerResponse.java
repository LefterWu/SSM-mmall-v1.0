package com.mmall.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @Description: 服务器响应对象，用于创建服务器响应
 * @author: wuleshen
 */
@JsonInclude(NON_EMPTY)
//只序列化非空对象，这样当字段中有空值时，不会被序列化
public class ServerResponse<T> implements Serializable {

    private int status;
    private String msg;
    private T data;

    /**
     * 私有化所有构造器，自定义公有方法构造调用构造器
     */
    private ServerResponse() {
    }

    private ServerResponse(int status) {
        this.status = status;
    }

    private ServerResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    private ServerResponse(int status, T data) {
        this.status = status;
        this.data = data;
    }

    private ServerResponse(int status, String msg, T data) {
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    @JsonIgnore
    //可以避免序列化成json对象
    public boolean isSuccuess() {
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public int getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }

    /**
     * 创建成功响应对象
     * @return ServerResponse对象，status=0
     */
    public static <T> ServerResponse<T> createBySuccess() {
        return new ServerResponse<>(ResponseCode.SUCCESS.getCode());
    }

    /**
     * 创建带消息的成功响应对象
     * @param msg 消息
     * @return ServerResponse对象，status=0，msg为传入参数
     */
    public static <T> ServerResponse<T> createBySuccessMessage(String msg) {
        return new ServerResponse<>(ResponseCode.SUCCESS.getCode(), msg);
    }

    /**
     * 创建带消息和数据的成功响应对象
     * @param msg 消息
     * @param data 数据
     * @param <T> 数据类型
     * @return ServerResponse对象，status=0，msg和data为传入参数
     */
    public static <T> ServerResponse<T> createBySuccess(String msg, T data) {
        return new ServerResponse<>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    /**
     * 创建带数据的成功响应对象
     * @param data 数据
     * @param <T> 数据类型
     * @return ServerResponse对象，status=0，data为传入参数
     */
    public static <T> ServerResponse<T> createBySuccess(T data) {
        return new ServerResponse<>(ResponseCode.SUCCESS.getCode(), data);
    }

    /**
     * 创建失败响应对象
     * @return ServerResponse对象，status=1
     */
    public static <T> ServerResponse<T> createByError() {
        return new ServerResponse<>(ResponseCode.ERROR.getCode());
    }

    /**
     * 创建带消息的默认失败响应对象
     * @param errorMessage 消息
     * @return ServerResponse对象，status=1，msg为传入参数
     */
    public static <T> ServerResponse<T> createByErrorMessage(String errorMessage) {
        return new ServerResponse<>(ResponseCode.ERROR.getCode(), errorMessage);
    }

    /**
     * 创建带消息的失败响应对象
     * @param errorCode 失败状态码
     * @param errorMessage 失败消息
     * @return ServerResponse对象，status=2 or 10，errorCode和errorMessage为传入参数
     */
    public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode, String errorMessage) {
        return new ServerResponse<>(errorCode, errorMessage);
    }
}
