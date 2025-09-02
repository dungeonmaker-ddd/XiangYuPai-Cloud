package com.xypai.common.core.domain;

import java.io.Serializable;
import com.xypai.common.core.constant.Constants;

/**
 * 响应信息主体
 *
 * @author ruoyi
 */
public class R<T> implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 成功 */
    public static final int SUCCESS = Constants.SUCCESS;

    /** 失败 */
    public static final int FAIL = Constants.FAIL;

    private int code;

    private String msg;

    private T data;

    public static <T> R<T> ok()
    {
        return restResult(null, SUCCESS, null);
    }

    public static <T> R<T> ok(T data)
    {
        return restResult(data, SUCCESS, null);
    }

    public static <T> R<T> ok(T data, String msg)
    {
        return restResult(data, SUCCESS, msg);
    }

    public static <T> R<T> fail()
    {
        return restResult(null, FAIL, null);
    }

    public static <T> R<T> fail(String msg)
    {
        return restResult(null, FAIL, msg);
    }

    public static <T> R<T> fail(T data)
    {
        return restResult(data, FAIL, null);
    }

    public static <T> R<T> fail(T data, String msg)
    {
        return restResult(data, FAIL, msg);
    }

    public static <T> R<T> fail(int code, String msg)
    {
        return restResult(null, code, msg);
    }

    private static <T> R<T> restResult(T data, int code, String msg)
    {
        R<T> apiResult = new R<>();
        apiResult.setCode(code);
        apiResult.setData(data);
        apiResult.setMsg(msg);
        return apiResult;
    }

    public int getCode()
    {
        return code;
    }

    public void setCode(int code)
    {
        this.code = code;
    }

    public String getMsg()
    {
        return msg;
    }

    public void setMsg(String msg)
    {
        this.msg = msg;
    }

    public T getData()
    {
        return data;
    }

    public void setData(T data)
    {
        this.data = data;
    }

    public static <T> Boolean isError(R<T> ret)
    {
        return !isSuccess(ret);
    }

    public static <T> Boolean isSuccess(R<T> ret)
    {
        return R.SUCCESS == ret.getCode();
    }

    /**
     * 根据布尔值返回结果
     * 
     * @param result 布尔结果
     * @return R<Void>
     */
    public static R<Void> result(boolean result)
    {
        return result ? ok() : fail();
    }

    /**
     * 根据布尔值返回结果，带自定义消息
     * 
     * @param result 布尔结果
     * @param successMsg 成功消息
     * @param failMsg 失败消息
     * @return R<Void>
     */
    public static R<Void> result(boolean result, String successMsg, String failMsg)
    {
        return result ? ok(null, successMsg) : fail(failMsg);
    }

    /**
     * 根据影响行数返回结果
     * 
     * @param rows 影响行数
     * @return R<Void>
     */
    public static R<Void> result(int rows)
    {
        return rows > 0 ? ok() : fail();
    }
}
