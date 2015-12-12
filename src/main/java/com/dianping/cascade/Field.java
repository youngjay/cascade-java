package com.dianping.cascade;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by yangjie on 9/22/15.
 */
@Data
public class Field {
    public final static String DEFAULT_CATEGORY = "query";

    private final static List<Field> EMPTY_CHILDREN = Lists.newArrayList();
    private final static List<String> ALL_PROPS = Lists.newArrayList();

    private String as;
    private String type;
    private String category = DEFAULT_CATEGORY;
    private Map params = Maps.newHashMap();
    private List<Field> children = EMPTY_CHILDREN;
    private List<String> props = ALL_PROPS;

    public String getComputedAs() {
        String as = getAs();

        if (StringUtils.isNotBlank(as)) {
            return as;
        }

        as = StringUtils.uncapitalize(getType());

        if (StringUtils.isNotBlank(getCategory()) && !getCategory().equals(DEFAULT_CATEGORY)) {
            as += "_" + getCategory();
        }

        return as;
    }
}
