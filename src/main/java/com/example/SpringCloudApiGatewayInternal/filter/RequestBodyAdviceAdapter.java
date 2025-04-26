package com.example.SpringCloudApiGatewayInternal.filter;

import com.example.SpringCloudApiGatewayInternal.model.BaseRequestDTO;
import com.example.SpringCloudApiGatewayInternal.utils.Const;
import com.example.SpringCloudApiGatewayInternal.utils.DataUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.RequestBodyAdvice;

import java.io.IOException;
import java.lang.reflect.Type;

@RestControllerAdvice
@Slf4j
public class RequestBodyAdviceAdapter implements RequestBodyAdvice {
    @Autowired
    private ObjectMapper objectMapper = new ObjectMapper();
    @Override
    public boolean supports(MethodParameter methodParameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public HttpInputMessage beforeBodyRead(HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) throws IOException {
        return inputMessage;
    }

    @Override
    public Object afterBodyRead(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        String targetClassName = body.getClass().getName();
        try {
            Class<?> targetClass = Class.forName(targetClassName);
            Object convertedObject = objectMapper.convertValue(body, targetClass);
            targetClass.cast(convertedObject);
            if(convertedObject instanceof BaseRequestDTO){
                String content = ((BaseRequestDTO) convertedObject).getContent();
                convertedObject = objectMapper.readValue(DataUtils.decrypt(Const.SECRET_AES_KEY, content,"AES"),targetClass);
            }
            return convertedObject;
        } catch (Exception e) {
            throw new RuntimeException("Error at afterBodyRead: " + targetClassName, e);
        }
    }

    @Override
    public Object handleEmptyBody(Object body, HttpInputMessage inputMessage, MethodParameter parameter, Type targetType, Class<? extends HttpMessageConverter<?>> converterType) {
        return body;
    }
}
