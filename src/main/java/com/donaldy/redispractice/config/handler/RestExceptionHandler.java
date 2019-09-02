package com.donaldy.redispractice.config.handler;

import com.donaldy.redispractice.common.Const;
import com.donaldy.redispractice.common.ServerResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.Iterator;

@Slf4j
@ControllerAdvice
public class RestExceptionHandler {

    /**
     * 自定义运行异常
     * @param exception 自定义异常
     * @return          异常信息
     */
    @ExceptionHandler(RestfulException.class)
    @ResponseBody
    public ServerResponse restfulExceptionHandler(RestfulException exception) {
        log.error(exception.getMessage());
        exception.printStackTrace();
        return ServerResponse.createByErrorCodeMessage(exception.getCode() ,exception.getMsg());
    }

    /**
     * 参数不完整(400)
     * @param exception 参数校验错误
     * @return          异常信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(code = HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ServerResponse methodExceptionHandler(MethodArgumentNotValidException exception) {
        log.error(exception.getMessage());
        exception.printStackTrace();
        return ServerResponse.createByErrorCodeMessage(Const.HttpStatusCode.BAD_REQUEST.getCode(),
                "这貌似不送给我的吧");
    }

    /**
     * 404的拦截.
     * @param exception 找不到路径
     * @return          异常信息
     */
    @ResponseStatus(code = HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseBody
    public ServerResponse notFoundHandler(NoHandlerFoundException exception) {
        log.error(exception.getMessage());
        exception.printStackTrace();
        return ServerResponse.createByErrorCodeMessage(Const.HttpStatusCode.NOT_FOUND.getCode(),
                "真没找到这个资源哇！");
    }

    /**
     * 参数格式错误(400)
     * @param exception 参数格式错误
     * @return          异常信息
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ServerResponse numberFormatHandler(MethodArgumentTypeMismatchException exception) {
        log.error(exception.getMessage());
        exception.printStackTrace();
        return ServerResponse.createByErrorCodeMessage(Const.HttpStatusCode.BAD_REQUEST.getCode(),
                "我要的是红色的，你怎么给我绿色。");
    }

    /**
     * 校验JSR-303
     * @param exception JSR校验失败
     * @return          异常信息
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ServerResponse validate(ConstraintViolationException exception) {
        log.error(exception.getMessage());
        exception.printStackTrace();
        Iterator iter = exception.getConstraintViolations().iterator();
        ConstraintViolation constraintViolation = (ConstraintViolation) iter.next();
        return ServerResponse.createByErrorCodeMessage(Const.HttpStatusCode.BAD_REQUEST.getCode(),
                constraintViolation.getConstraintDescriptor().getMessageTemplate());
    }

    /**
     * 缺少参数
     * @param exception 参数缺少
     * @return          异常信息
     */
    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseBody
    public ServerResponse requestMissingHandler(MissingServletRequestParameterException exception) {
        log.error(exception.getMessage());
        exception.printStackTrace();
        return ServerResponse.createByErrorCodeMessage(Const.HttpStatusCode.BAD_REQUEST.getCode() ,
                "这快递不是我的");
    }

    /**
     * IO异常(500)
     * @param exception IO异常
     * @return          异常信息
     */
    @ExceptionHandler(IOException.class)
    @ResponseBody
    public ServerResponse ioExceptionHandler(IOException exception) {
        log.error(exception.getMessage());
        exception.printStackTrace();
        return ServerResponse.createByErrorCodeMessage(Const.HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(),
                "快递送错了");
    }

    /**
     * 默认异常处理(500)
     * @param exception 其他错误
     * @return          异常信息
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ServerResponse serverErrorHandler(Exception exception) {
        log.error(exception.getMessage());
        log.error(String.valueOf(exception.getStackTrace()));
        exception.printStackTrace();
        return ServerResponse.createByErrorCodeMessage(Const.HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(),
                "好像出错了哦");
    }
}
