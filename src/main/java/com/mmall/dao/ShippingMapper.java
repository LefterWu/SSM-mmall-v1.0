package com.mmall.dao;

import com.mmall.pojo.Shipping;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ShippingMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Shipping record);

    int insertSelective(Shipping record);

    Shipping selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Shipping record);

    int updateByPrimaryKey(Shipping record);

    /**
     * 防止横向越权，根据用户id查询地址
    * @param shippingId 地址id
     * @param userId 用户id
     * @return
     */
    int deleteByShippingIdUserId(@Param("shippingId") Integer shippingId, @Param("userId") Integer userId);

    /**
     * 防止横向越权，根据用户更新地址
     * @param shipping 地址对象
     * @return
     */
    int updateByShippingUserId(Shipping shipping);

    /**
     * 查询地址
     * @param userId 用户id
     * @param shippingId 地址id
     * @return
     */
    Shipping selectByShippingIdUserId(@Param("userId") Integer userId, @Param("shippingId") Integer shippingId);

    /**
     * 查询用户的所有地址
     * @param userId 用户id
     * @return List
     */
    List<Shipping> selectAllByUserId(Integer userId);
}