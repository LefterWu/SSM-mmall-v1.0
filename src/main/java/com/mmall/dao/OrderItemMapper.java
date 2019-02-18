package com.mmall.dao;

import com.mmall.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface OrderItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(OrderItem record);

    int insertSelective(OrderItem record);

    OrderItem selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(OrderItem record);

    int updateByPrimaryKey(OrderItem record);

    /**
     * 获取用户订单详情
     * @param orderNo 订单号
     * @param userId 用户id
     * @return 订单详情list
     */
    List<OrderItem> getByOrderNoUserId(@Param("orderNo") Long orderNo, @Param("userId") Integer userId);

    /**
     * 批量插入
     * @param availableOrderItemList 订单商品list
     */
    int batchInsert(@Param("availableOrderItemList") List<OrderItem> availableOrderItemList);

    /**
     * 后台获取订单
     * @param orderNo
     * @return
     */
    List<OrderItem> getByOrderNo(Long orderNo);
}