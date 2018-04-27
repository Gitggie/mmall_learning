// package com.mmall.common;
//
// /**
//  * @Author Wuleijian
//  * @Date 2018/4/2 11:45
//  * @Description
//  */
// public enum ResponseCode {
//
//     SUCCESS(0, "SUCCESS"),
//
//     ERROR(1, "ERROR"),
//
//     NEED_LOGIN(10, "NEED_LOGIN"),
//
//     ILLEGAL_ARGUMENT(2, "ILLEGAL_ARGUMENT");
//
//     private final int code;
//     private final String desc;
//
//     ResponseCode(int code, String desc) {
//         this.code = code;
//         this.desc = desc;
//     }
//
//     public int getCode() {
//         return code;
//     }
//
//     public String getDesc() {
//         return desc;
//     }
// }
package com.mmall.common;

public enum ResponseCode {
    SUCCESS(0, "SUCCESS"),
    ERROR(1, "ERROR"),
    NEED_LOGIN(10, "NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2, "ILLEGAL_ARGUMENT");

    private final int code;
    private final String desc;

    //这个构造方法是为枚举变量服务的
    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

