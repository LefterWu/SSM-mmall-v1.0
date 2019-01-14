package com.mmall.vo;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: 购物车的vo对象
 * @author: wuleshen
 */
public class CartVo {

    private List<CartProductVo> cartProductVoList;  //封装了购物出商品vo对象
    private BigDecimal cartTotalPrice;  //购物车的总价
    private Boolean allChecked;     //是否全部勾选
    private String imageHost;       //购物车商品图片host

    public List<CartProductVo> getCartProductVoList() {
        return cartProductVoList;
    }

    public void setCartProductVoList(List<CartProductVo> cartProductVoList) {
        this.cartProductVoList = cartProductVoList;
    }

    public BigDecimal getCartTotalPrice() {
        return cartTotalPrice;
    }

    public void setCartTotalPrice(BigDecimal cartTotalPrice) {
        this.cartTotalPrice = cartTotalPrice;
    }

    public Boolean getAllChecked() {
        return allChecked;
    }

    public void setAllChecked(Boolean allChecked) {
        this.allChecked = allChecked;
    }

    public String getImageHost() {
        return imageHost;
    }

    public void setImageHost(String imageHost) {
        this.imageHost = imageHost;
    }
}
