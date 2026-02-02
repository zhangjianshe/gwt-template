package cn.mapway.gwt_template.shared.rpc.file;

import lombok.Data;

import java.io.Serializable;

/**
 * UploadReturnResponse
 *
 * @author zhangjianshe@gmail.com
 */
@Data
public class UploadReturnResponse implements Serializable {

    /**
     * 客户端上传的 extra数据.
     */
    public String extra;
    /**
     * 返回代码 0成功 其他错误.
     */
    public int retCode;
    /**
     * 错误消息.
     */
    public String msg;
    /**
     * 相对路径.
     */
    public String relPath;
    /**
     * MD5.
     */
    public String md5;
    /**
     * 文件大小.
     */
    public long size;
    /**
     * 原始文件名
     */
    public String fileName;


}
