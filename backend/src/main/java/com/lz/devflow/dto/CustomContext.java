package com.lz.devflow.dto;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;


@Component
@RequestScope
public class CustomContext {
    private String userDID;

    public CustomContext() {
    }

    public String getUserDID() {
        return userDID;
    }

    public void setUserDID(String userDID) {
        this.userDID = userDID;
    }
}
