package com.example.pitchboxd.auth.presentation;

import com.example.pitchboxd.global.exception.BusinessException;
import com.example.pitchboxd.global.exception.ErrorCode;
import com.example.pitchboxd.global.security.UserAdaptor;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Component
public class LoginUserIdResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginUserId.class) &&
                parameter.getParameterType().equals(Long.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

        LoginUserId annotation = parameter.getParameterAnnotation(LoginUserId.class);
        boolean isRequired = annotation != null && annotation.required();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 2. 비로그인 상태(익명 사용자) 확인
        boolean isAnonymous = (authentication == null || authentication instanceof AnonymousAuthenticationToken);

        if (isAnonymous) {
            if (isRequired) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            } else {
                return null;
            }
        }

        UserAdaptor userAdaptor = (UserAdaptor) authentication.getPrincipal();
        return userAdaptor.getUserId();
    }
}
