package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Shipping;

import java.util.Map;

/**
 * @Description: 收货地址模块
 * @author: wuleshen
 */
public interface IShippingService {

    ServerResponse<Map> add(Integer userId, Shipping shipping);

    ServerResponse<String> delete(Integer id, Integer shippingId);

    ServerResponse<String> update(Integer id, Shipping shipping);

    ServerResponse<Shipping> select(Integer id, Integer shippingId);

    ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize);
}
