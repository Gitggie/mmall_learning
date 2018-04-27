package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

/**
 * @Author Wuleijian
 * @Date 2018/4/2 11:29
 * @Description
 */
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
//保证序列化json的时候,如果是null的对象,key也会消失
public class ServerResponse<T> implements Serializable {

    private int status;
    private String msg;
    private T data;

    private ServerResponse(int status) {
        this.status = status;
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

    private ServerResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    @JsonIgnore
    //使之不在json序列化结果当中
    public boolean isSuccess() {
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    public int getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getMsg() {
        return msg;
    }

    public static <T> ServerResponse<T> createBySuccess() {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }

    public static <T> ServerResponse<T> createBySuccessMessage(String msg) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
    }

    public static <T> ServerResponse<T> createBySuccess(T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), data);
    }

    public static <T> ServerResponse<T> createBySuccess(String msg, T data) {
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
    }

    public static <T> ServerResponse<T> createByError() {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDesc());
    }

    public static <T> ServerResponse<T> createByErrorMessage(String errorMessage) {
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(), errorMessage);
    }

    public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode, String errorMessage) {
        return new ServerResponse<T>(errorCode, errorMessage);
    }
}
// package com.mmall.common;
//
// import org.codehaus.jackson.annotate.JsonIgnore;
// import org.codehaus.jackson.map.annotate.JsonSerialize;
//
// import java.io.Serializable;
//
// //ServerResponse<T>对象序列化为json时，key值为null时，该key值在前端不显示
// @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
// public class ServerResponse<T> implements Serializable {
//
//     private int status;
//     private String msg;
//     private T data;
//
//     //参数里面永远有status
//     private ServerResponse(int status) {
//         this.status = status;
//     }
//
//     private ServerResponse(int status, String msg) {
//         this.status = status;
//         this.msg = msg;
//     }
//
//     private ServerResponse(int status, T data) {
//         this.status = status;
//         this.data = data;
//     }
//
//     private ServerResponse(int status, String msg, T data) {
//         this.status = status;
//         this.msg = msg;
//         this.data = data;
//     }
//
//     //使返回值不在json序列化结果中
//     @JsonIgnore
//     public boolean isSuccess() {
//         //没写成this.status
//         //返回的是（status是否等于0，因为status是int类型，默认值为0，所以结果为true）
//         //那为什么不直接写return true？因为status是还有可能是其他值？参考login的返回status状态
//         return status == ResponseCode.SUCCESS.getCode();
//     }
//
//     public int getStatus() {
//         return status;
//     }
//
//     public String getMsg() {
//         return msg;
//     }
//
//     public T getData() {
//         return data;
//     }
//
//     //第一个<T>是为了告诉编译器接下来要使用泛型
//     //为什么返回要用new修饰？因为是构造器呀？要返回一个新建实例对象
//     public static <T> ServerResponse<T> createBySuccess() {
//         return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
//     }
//
//     public static <T> ServerResponse<T> createBySuccess(T data) {
//         return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), data);
//     }
//
//     public static <T> ServerResponse<T> createBySuccess(String msg, T data) {
//         return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg, data);
//     }
//
//     public static <T> ServerResponse<T> createBySuccessMessage(String msg) {
//         return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(), msg);
//     }
//
//     public static <T> ServerResponse<T> createByError() {
//         return new ServerResponse<T>(ResponseCode.ERROR.getCode(), ResponseCode.ERROR.getDesc());
//     }
//
//     public static <T> ServerResponse<T> createByErrorMessage(String errorMessage) {
//         return new ServerResponse<T>(ResponseCode.ERROR.getCode(), errorMessage);
//     }
//
//     public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode, String errorMessage) {
//         return new ServerResponse<T>(errorCode, errorMessage);
//     }
//
// }