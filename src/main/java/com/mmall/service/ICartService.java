package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.vo.CartVo;

/**
 * @Description: 购物车服务
 * @author: wuleshen
 */
public interface ICartService {

    /**
     * 添加购物车
     * @param userId 用户id
     * @param productId 商品id
     * @param count 商品数量
     * @return
     */
    ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count);

    /**
     * 更新购物车
     * @param userId 用户id
     * @param productId 商品id
     * @param count 商品数量
     * @return
     */
    ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count);

    /**
     * 删除购物车商品
     * @param userId 用户id
     * @param productIds 要删除的商品id字符串，用逗号分隔
     * @return
     */
    ServerResponse<CartVo> delete(Integer userId, String productIds);

    /**
     * 列出购物车所有商品
     * @param userId 用户id
     * @return
     */
    ServerResponse<CartVo> list(Integer userId);


    /**
     * 选择或反选，dao层根据productId是否为空进行单选或全选的操作
     * @param userId 用户id
     * @param checked 选择状态
     * @param productId 商品id，为空，则进行全选；不空，则进行单选
     * @return
     */
    ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer checked, Integer productId);

    /**
     * 获取购物车商品总数
     * @param userId 用户id
     * @return
     */
    ServerResponse<Integer> getCartProductCount(Integer userId);
}
