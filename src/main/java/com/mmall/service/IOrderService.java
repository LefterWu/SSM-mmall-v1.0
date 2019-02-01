package com.mmall.service;

import com.mmall.common.ServerResponse;

import java.util.Map;

/**
 * @Description: 订单接口
 * @author: wuleshen
 */
public interface IOrderService {

    ServerResponse pay(Long orderNo, Integer userId, String path);

    ServerResponse alipayCallback(Map<String, String> params);

    ServerResponse queryOrderPayStatus(Integer userId, Long orderNo);
}
