package com.dianping.cascade.test.cascade;

import com.dianping.cascade.annotation.Param;
import com.dianping.cascade.test.model.Context;
import com.dianping.cascade.test.model.UserDTO;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by yangjie on 9/22/15.
 */
public class User {
    public List<UserDTO> query(@Param("name") String name, @Param("context") Context context) {
        return Lists.newArrayList(new UserDTO(1, name), new UserDTO(2, context.getName()));
    }
}
