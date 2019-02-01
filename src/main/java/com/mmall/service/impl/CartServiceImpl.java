package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CartMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Cart;
import com.mmall.pojo.Product;
import com.mmall.service.ICartService;
import com.mmall.util.BigDecimalUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.CartProductVo;
import com.mmall.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: 购物车业务
 * @author: wuleshen
 */
@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart = cartMapper.selectByUserIdProductId(userId,productId);
        if (cart == null) {
            //如果商品不在购物车里，在购物车中添加该商品
            Cart cartItem = new Cart();
            cartItem.setUserId(userId);
            cartItem.setProductId(productId);
            cartItem.setQuantity(count);
            cartItem.setChecked(Const.Cart.CHECKED);    //将购物车设置成选中状态
            cartMapper.insertSelective(cartItem);
        } else {
            //如果商品在购物车，则增加商品数量(后续通过#getCartVoLimit方法判断库存是否充足)
            cart.setQuantity(cart.getQuantity() + count);
            cartMapper.updateByPrimaryKeySelective(cart);
        }
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> update(Integer userId, Integer productId, Integer count) {
        if (productId == null || count == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectByUserIdProductId(userId,productId);
        //更新购物车数量
        if (cart != null) {
            cart.setQuantity(count);
        } else {
            //如果查询不到相应购物车，返回错误参数
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.updateByPrimaryKeySelective(cart);
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> delete(Integer userId, String productIds) {
        //分隔要删除的productIds字符串，放入一个list
        List<Integer> productIdList = new ArrayList<>();
        String[] ids = productIds.split(",");
        for (String strItem: ids) {
            productIdList.add(Integer.parseInt(strItem.trim()));
        }
        if (productIdList.isEmpty()) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //执行删除
        cartMapper.deleteProductByProductIds(userId, productIdList);
        return this.list(userId);
    }

    @Override
    public ServerResponse<CartVo> list(Integer userId) {
        CartVo cartVo = getCartVoLimit(userId);
        return ServerResponse.createBySuccess(cartVo);
        //todo 这里不进行分页，后续可追加购物车分页功能
    }

    @Override
    public ServerResponse<CartVo> selectOrUnSelect(Integer userId, Integer checked, Integer productId) {
        cartMapper.checkedOrUncheckedProduct(userId, checked, productId);
        //复用list方法
        return this.list(userId);
    }

    @Override
    public ServerResponse<Integer> getCartProductCount(Integer userId) {
        if (userId == null) {
            //如果参数出错，则返回数量0
            return ServerResponse.createBySuccess(0);
        }
        Integer count = cartMapper.selectCartProductCount(userId);
        return ServerResponse.createBySuccess(count);

        /* 另一种方式，通过遍历购物车累加quantity，相比直接sql查询效率更低？
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        int totalQuantity = 0;  //购物车商品总数量
        for (Cart cart: cartList) {
            int quantity = cart.getQuantity();
            totalQuantity += quantity;
        }
        return ServerResponse.createBySuccess(totalQuantity);*/
    }

    /**
     * 得到购物车VO对象
     * @param userId 用户id
     * @return 封装好的CartVo对象
     */
    private CartVo getCartVoLimit(Integer userId) {
        CartVo cartVo = new CartVo();
        //获取用户购物车中的所有商品
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);
        //用BigDecimal类储存购物车总价
        BigDecimal cartTotalPrice = new BigDecimal("0");
        Boolean isChecked = true;

        //将CartProductVo封装到CartProductVoList
        List<CartProductVo> cartProductVoList = new ArrayList<>();
        for (Cart cart: cartList) {
            //设置购物车相关属性
            CartProductVo cartProductVo = new CartProductVo();
            cartProductVo.setId(cart.getId());
            cartProductVo.setUserId(userId);
            cartProductVo.setProductId(cart.getProductId());
            //查询该条目的商品
            Product product = productMapper.selectByPrimaryKey(cart.getProductId());
            if (product != null) {
                //设置商品相关属性
                cartProductVo.setProductName(product.getName());
                cartProductVo.setProductSubtitle(product.getSubtitle());
                cartProductVo.setProductMainImage(product.getMainImage());
                cartProductVo.setProductPrice(product.getPrice());
                cartProductVo.setProductStock(product.getStock());
                cartProductVo.setProductStatus(product.getStatus());

                int buyLimitCount = 0;  //限制数量，如果库存充足，该值等于购物车中的数量；如果库存不足，则限制数量为当前库存数量
                //判断并设置库存限制
                if (product.getStock() >= cart.getQuantity()) {
                    //如果库存充足
                    cartProductVo.setLimitQuantity(Const.Cart.SUFFICIENT_STOCK);
                    buyLimitCount = cart.getQuantity();
                } else {
                    //如果库存不足
                    cartProductVo.setLimitQuantity(Const.Cart.INSUFFICIENT_STOCK);
                    //购物车中更新有效库存
                    buyLimitCount = product.getStock();
                    Cart cartForQuantity = new Cart();
                    cartForQuantity.setId(cart.getId());
                    cartForQuantity.setQuantity(buyLimitCount);
                    cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                }
                //设置购物车中的数量
                cartProductVo.setQuantity(buyLimitCount);
                //计算商品总价
                cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cart.getQuantity()));
                cartProductVo.setProductChecked(cart.getChecked());
            }
            //如果商品被勾选，则计算到购物车总价中
            if (cart.getChecked() == Const.Cart.CHECKED) {
                cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(), cartProductVo.getProductTotalPrice().doubleValue());
            }
            cartProductVoList.add(cartProductVo);
        }
        //装配cartvo
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setAllChecked(getAllCheckedStatus(userId));  //获取商品是否全选
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));
        return cartVo;
    }

    /**
     * 获取购物车商品是否全部勾选
     * @param userId
     * @return 全部勾选，返回true；否则，返回false
     */
    private boolean getAllCheckedStatus(Integer userId) {
        if (userId == null) {
            return false;
        }
        //通过sql查询出该用户所有的未勾选商品数量，如果等于0，说明全选
        return cartMapper.selectUncheckedByUserId(userId) == 0;
    }
}
