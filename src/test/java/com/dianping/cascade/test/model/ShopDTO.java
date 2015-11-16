package com.dianping.cascade.test.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by yangjie on 9/22/15.
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShopDTO {
    private int id;
    private String name;
    private int ownerId;
}
