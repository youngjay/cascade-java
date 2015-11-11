package com.dianping.cascade.test.cascade;

import com.dianping.cascade.annotation.Param;
import com.dianping.cascade.test.model.CooperationDTO;
import com.dianping.cascade.test.model.ShopDTO;

/**
 * Created by yangjie on 9/22/15.
 */
public class Cooperation {
    public CooperationDTO query(@Param("id") int id) {
        return new CooperationDTO(id * 10, "Cooperation for shop:" + id);
    }

    public CooperationDTO queryByShop(@Param("shop") ShopDTO shop) {
        return new CooperationDTO(shop.getId() * 10, "Cooperation for shop:" + shop.getId());
    }
}
