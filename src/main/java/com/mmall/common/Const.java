package com.mmall.common;

import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @Description: 存放一些常量
 * @author: wuleshen
 */
public class Const {
    public static final String CURRENT_USER = "currentUser";

    public static final String USERNAME = "username";
    public static final String EMAIL = "email";

    public interface Role {
        int ROLE_CUSTOMER = 0;      //普通用户
        int ROLE_ADMIN = 1;         //管理员
    }

    public interface SALE_STATUS {
        int ON_SALE = 1;    //在售
        int SOLD_OUT = 2;   //售完或下架
        int DELETE = 3;     //删除
    }

    public interface ORDER_BY {
        Set<String> PRICE_ORDER = Sets.newHashSet("price_desc", "price_asc");
    }

    public interface Cart {
        int CHECKED = 1;    //购物车选中状态
        int UN_CHECKED = 0; //购物车未选中状态
    }
}
