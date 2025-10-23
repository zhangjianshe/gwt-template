package cn.mapway.gwt_template.shared;

import cn.mapway.document.annotation.ApiField;
import cn.mapway.document.annotation.Doc;
import com.google.gwt.user.client.rpc.IsSerializable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

/**
 * 系统对外API的返回结果包装类
 *
 * @author zhangjs2@ziroom.com
 */
@Getter
@Setter
@Doc(value = "API接口封装包")
public class ApiResult<T> implements Serializable, IsSerializable {
    /**
     * CODE 表示本次接口调用的返回码，返回码分类两大类，第一类是 公用的返回结果，第二类是业务返回结果
     * 第一类目前定义了2个   200 调用成功   500 服务器错误，未知的运行时错误
     * 第二类代码 用8位数字表示  XXXXXXXX 前4位为子系统编码 后四位为子系统内部编码(内部编码可以按照模块进行细分)
     * 其他子系统 请联系 zhangjs2@ziroom.com 制定代码
     */
    @ApiField(value = "返回代码,200为处理正确的结果，其他为错误结果", example = "200")
    private Integer code;

    /**
     * 给客户端返回的必要提示信息，如果是业务出错信息，最好消息上添加上解决问题的方法和建议
     */
    @ApiField(value = "返回代码部位200时，此字段返回错误的具体原因", example = "给客户端返回的必要提示信息，如果是业务出错信息，最好消息上添加上解决问题的方法和建议")
    private String message;

    /**
     * 各个接口返回的属于各个接口的返回数据
     */
    @ApiField(value = "返回的具体类型")
    private T data;


    /**
     * @param code    daode
     * @param message message
     * @param data    data
     * @param <T>     data type
     * @return data
     */
    public static <T> ApiResult<T> result(Integer code, String message, T data) {
        ApiResult result = new ApiResult();
        result.setCode(code);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 创建空的返回结果集，使用者需要填写 code message data 三个字段值
     *
     * @return data
     */
    public static ApiResult create() {
        return new ApiResult();
    }

    /**
     * 返回代码Code 为200的代码返回结果
     *
     * @param data data to
     * @param <T>  datatype
     * @return data
     */
    public static <T> ApiResult success(T data) {
        ApiResult result = result(200, null, data);
        result.setData(data);
        return result;
    }


    /**
     * 返回代码Code 为500的代码返回结果
     *
     * @param message 500异常信息
     * @return data
     */
    public static ApiResult error(String message) {
        return result(500, null, message);
    }

    /**
     * 是否成功
     *
     * @return
     */
    public boolean isSuccess() {
        return Objects.equals(code, 200);
    }

    /**
     * 是否失败
     *
     * @return
     */
    public boolean isFailed() {
        return !isSuccess();
    }
}
