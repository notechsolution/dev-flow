package com.lz.devflow.constant;

public class CommonConstant {
    public static final String CUSTOM_REQUEST_HEADER = "x-request-user-did";
    public static final Integer SDK_DEFAULT_SUCCESS_CODE = 200000;
    public static final String ROLE_WEBHOOK = "WEBHOOK";
    public static final String ROLE_JENKINS = "JENKINS";


    public static boolean isSDKInvokedSuccess(Integer code) {
        return code != null && code.equals(SDK_DEFAULT_SUCCESS_CODE);
    }
}
