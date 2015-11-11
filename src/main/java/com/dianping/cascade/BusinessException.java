package com.dianping.cascade;

/**
 * Created by yangjie on 11/11/15.
 * 抛出次异常时，不会显示抛出的地点，适用于业务异常
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String s){super(s);}
}
