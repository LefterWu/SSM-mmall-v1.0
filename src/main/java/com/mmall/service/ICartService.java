package com.mmall.service;

import com.mmall.common.ServerResponse;

/**
 * @Description: 购物车服务
 * @author: wuleshen
 */
public interface ICartService {

    /**
     * 添加购物车
     * @param userId
     * @param productId
     * @param count 商品数量
     * @return
     */
    ServerResponse add(Integer userId, Integer productId, Integer count);
}
