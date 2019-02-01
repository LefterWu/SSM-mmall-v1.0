package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.dao.ShippingMapper;
import com.mmall.pojo.Shipping;
import com.mmall.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 收货地址模块
 * @author: wuleshen
 */
@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    @Override
    public ServerResponse<Map> add(Integer userId, Shipping shipping) {
        shipping.setUserId(userId);
        // 插入完成后要拿到shipping的主键id
        int rowCount = shippingMapper.insert(shipping);
        if (rowCount > 0) {
            Map result = new HashMap();
            result.put("shippingId", shipping.getId());
            return ServerResponse.createBySuccess("新增地址成功", result);
        }
        return ServerResponse.createByErrorMessage("新增地址失败");
    }

    @Override
    public ServerResponse<String> delete(Integer userId, Integer shippingId) {
        //注意，这里会有横向越权的问题，需要进行防范。如果登录用户直接传入一个不属于该用户的shippingId，那么就会发生横向越权
        int rowCount = shippingMapper.deleteByShippingIdUserId(shippingId, userId);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    @Override
    public ServerResponse<String> update(Integer userId, Shipping shipping) {
        //设置shipping的用户id为登录用户的id，防止后面更新地址时接收到别的userId，避免横向越权
        shipping.setUserId(userId);
        //防止横向越权，更新地址时判断用户id
        int rowCount = shippingMapper.updateByShippingUserId(shipping);
        if (rowCount > 0) {
            return ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    @Override
    public ServerResponse<Shipping> select(Integer userId, Integer shippingId) {
        Shipping resultShipping = shippingMapper.selectByShippingIdUserId(userId, shippingId);
        if (resultShipping == null) {
            return ServerResponse.createByErrorMessage("查询不到该地址");
        }
        return ServerResponse.createBySuccess("查询地址成功", resultShipping);
    }

    @Override
    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Shipping> resultShippingList = shippingMapper.selectAllByUserId(userId);
        PageInfo pageInfo = new PageInfo(resultShippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
