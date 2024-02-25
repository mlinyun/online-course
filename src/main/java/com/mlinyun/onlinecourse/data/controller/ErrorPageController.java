package com.mlinyun.onlinecourse.data.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * 错误页面配置方案，这里做了 3 个错误页面，
 * 分别是 400 、404 和 5xx 的错误页面
 */
@Controller
public class ErrorPageController implements ErrorViewResolver {

    private static ErrorPageController errorPageController;

    // 在这里不要使用 @Autowired 注解，会报错
    @Qualifier
    private ErrorAttributes errorAttributes;

    public ErrorPageController(ErrorAttributes errorAttributes) {
        this.errorAttributes = errorAttributes;
    }

    public ErrorPageController() {
        if (errorPageController == null) {
            errorPageController = new ErrorPageController(errorAttributes);
        }
    }

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status, Map<String, Object> model) {
        if (HttpStatus.BAD_REQUEST == status) {
            return new ModelAndView("error/error_400");
        } else if (HttpStatus.NOT_FOUND == status) {
            return new ModelAndView("error/error_404");
        } else {
            return new ModelAndView("error/error_5xx");
        }
    }

}
