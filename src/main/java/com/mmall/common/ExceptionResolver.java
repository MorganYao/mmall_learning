package com.mmall.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author : pengyao
 * @Date: 2018/7/3 23: 42
 */
@Slf4j
//注入到spring容器中
@Component
public class ExceptionResolver implements HandlerExceptionResolver{
    @Override
    public ModelAndView resolveException(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) {
        //此行错误日志一定要打印，会显示请求错误的具体URI,便于排查错误
        log.error("{} Exception",httpServletRequest.getRequestURI(),e);
        //当使用jackson2.x时使用MappingJackson2JsonView，此项目使用的是Jackson1.9
        ModelAndView modelAndView = new ModelAndView(new MappingJacksonJsonView());

        //根据ServiceResponse类，添加modleandView的值，返回ModelAndView
        modelAndView.addObject("status",ResponseCode.ERROR.getCode());
        modelAndView.addObject("msg","接口异常，详情请查看服务端日志");
        modelAndView.addObject("data",e.toString());//向前端返回简明扼要的异常信息
        return modelAndView;
    }
}
