package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.pojo.Cart;
import com.mmall.service.ICartService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Description: TODO
 * @author: wuleshen
 */
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Override
    public ServerResponse add(Integer userId, Integer productId, Integer count) {
        Cart cart = cartMapper.selectByUserIdProductId(userId,productId);
        if (cart == null) {
            //如果商品不在购物车里，添加该商品
            Cart cartItem = new Cart();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);    //将购物车设置成选中状态
            cartMapper.insertSelective(cartItem);
        }
        return null;
    }
}
