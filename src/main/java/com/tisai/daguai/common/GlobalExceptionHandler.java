package com.tisai.daguai.common;

import com.tisai.daguai.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理
 */
//@ControllerAdvice控制类通知,annotations意思是注解,即用来规定拦截哪些注解的类
@ControllerAdvice(annotations = {RestController.class, Controller.class})
//因为类中有些数据需要以json格式返回
@ResponseBody
@Slf4j
public class GlobalExceptionHandler {
    /**
     * ExceptionHandler
     * 异常处理方法,当捕捉到异常SQLIntegrityConstraintViolationException就会调用这个方法处理
     * @return r
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result exceptionHandler(SQLIntegrityConstraintViolationException ex){
        log.error(ex.getMessage());
        //判断错误信息中是否包含Duplicate entry来进一步锁定具体错误
        //Duplicate entry: 唯一变量重复错误
        if(ex.getMessage().contains("Duplicate entry")){
            //String的方法split,根据特定字符分割字符串并依次存储到数组中
            String[] split = ex.getMessage().split(" ");
            //msg = 'xxx'已存在
            String msg= split[2] + "已存在";
            return Result.fail(msg);
        }
        return Result.fail("未知错误");
    }

    /**
     * 业务异常处理
     * ExceptionHandler
     * @return r
     */
    @ExceptionHandler(CustomException.class)
    public Result exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        return Result.fail(ex.getMessage());
    }

    /**
     * 系统异常处理
     * ExceptionHandler
     * @return r
     */
    @ExceptionHandler(SystemException.class)
    public Result exceptionHandler(SystemException ex){
        log.error(ex.getMessage()+ex.getCause());
        return Result.fail("系统出了点小问题");
    }
}
