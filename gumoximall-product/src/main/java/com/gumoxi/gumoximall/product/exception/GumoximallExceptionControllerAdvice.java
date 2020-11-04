package com.gumoxi.gumoximall.product.exception;

import com.gumoxi.gumoximall.common.exception.BizCodeEnume;
import com.gumoxi.gumoximall.common.utils.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice(value = "com.gumoxi.gumoximall.product.controller")
public class GumoximallExceptionControllerAdvice {

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public R handleVaildException(MethodArgumentNotValidException e) {
        log.error("数据校验出现错误{}，异常类型：{}", e.getMessage(), e.getClass());
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> map = new HashMap<String, String>();
        bindingResult.getFieldErrors().forEach((fieldError)->{
            map.put(fieldError.getField(), fieldError.getDefaultMessage());
        });
        return R.error(BizCodeEnume.VALID_EXCEPTION.getCode(),BizCodeEnume.VALID_EXCEPTION.getMsg()).put("data", map);
    }

    @ExceptionHandler(value = Throwable.class)
    public R handleException(Throwable e) {
        log.error("错误：", e);
        return R.error(BizCodeEnume.UNKNOW_EXCEPITON.getCode(),BizCodeEnume.UNKNOW_EXCEPITON.getMsg());
    }
}
