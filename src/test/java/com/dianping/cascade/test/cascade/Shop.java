package com.dianping.cascade.test.cascade;

import com.dianping.cascade.annotation.Param;
import com.dianping.cascade.test.model.ShopDTO;

/**
 * Created by yangjie on 9/22/15.
 */
public class Shop {
    public ShopDTO query(@Param("id") int id) {
        return new ShopDTO(id, "shopxxx");
    }

}
