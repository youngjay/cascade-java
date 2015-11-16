package com.dianping.cascade.test.cascade;

import com.dianping.cascade.BusinessException;
import com.dianping.cascade.annotation.Param;
import com.dianping.cascade.test.model.Context;
import com.dianping.cascade.test.model.UserDTO;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by yangjie on 9/22/15.
 */
public class User {
    public List<UserDTO> query() {
        return Lists.newArrayList(new UserDTO(1, "Jay"), new UserDTO(2, "Tom"));
    }

    public UserDTO load(@Param("userId") int id) {
        return new UserDTO(id, "Someone");
    }

    public int context(@Param("context") int context) {
        return context;
    }

    public Object businessException() {
        throw new BusinessException("error");
    }


    public Object runtimeException() {
        throw new RuntimeException("error");
    }
}
