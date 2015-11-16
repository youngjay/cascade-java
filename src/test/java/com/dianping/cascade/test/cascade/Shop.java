package com.dianping.cascade.test.cascade;

import com.dianping.cascade.annotation.Param;
import com.dianping.cascade.test.model.ShopDTO;

/**
 * Created by yangjie on 9/22/15.
 */
public class Shop {
    public ShopDTO query(@Param("id") int id) {
        return new ShopDTO(id, "shopxxx", 0);
    }

    public ShopDTO byUser(@Param("userId") int userId) {
        return new ShopDTO(1, "shop", userId);
    }


    public ShopDTO byUserName(@Param("userId") int userId, @Param("name") String userName) {
        return new ShopDTO(1, "user:" + userName, userId);
    }
}
