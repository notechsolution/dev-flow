package com.lz.devflow.dto.zentao;

/**
 * Generic Zentao API Response Wrapper
 */
public class ZentaoApiResponse<T> {
    
    private String status;
    private String message;
    private T data;
    private String error;
    
    public ZentaoApiResponse() {
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public T getData() {
        return data;
    }
    
    public void setData(T data) {
        this.data = data;
    }
    
    public String getError() {
        return error;
    }
    
    public void setError(String error) {
        this.error = error;
    }
    
    public boolean isSuccess() {
        return "success".equals(status);
    }
}
