package com.lz.devflow.configuration;

import static com.lz.devflow.constant.CommonConstant.CUSTOM_REQUEST_HEADER;

import com.lz.devflow.dto.CustomContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class RequestInterceptor implements HandlerInterceptor {
  private final Logger log = LoggerFactory.getLogger(RequestInterceptor.class);

  private final CustomContext customContext;

  public RequestInterceptor(CustomContext customContext) {
    this.customContext = customContext;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String userDID = request.getHeader(CUSTOM_REQUEST_HEADER);
    if (userDID == null) {
      log.error("RequestURI({}, - {}), userDID is null", request.getRequestURI(), request.getMethod());
      throw new Exception("No permission");
    }
    customContext.setUserDID(userDID);
    return true;
  }
}
