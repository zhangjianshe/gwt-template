package cn.mapway.gwt_template.server.config.security;

import cn.mapway.biz.api.ApiResult;
import cn.mapway.biz.exception.BizException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * ControllerExceptionHandler
 *
 * @author zhangjianshe <zhangjianshe@gmail.com>
 */
@ControllerAdvice
public class ControllerExceptionHandler {

    @ResponseBody
    @ExceptionHandler(value = MaxUploadSizeExceededException.class)
    public ApiResult catchFileSizeException(MaxUploadSizeExceededException ex) {
        return ApiResult.result(500, "文件超过上传最大值", null);
    }

    @ResponseBody
    @ExceptionHandler(value = BizException.class)
    public ApiResult catchBizException(BizException ex) {
        return ApiResult.result(ex.getCode(), ex.getMessage(), null);
    }
}
