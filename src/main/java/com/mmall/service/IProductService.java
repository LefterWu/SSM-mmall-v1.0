package com.mmall.service;

import com.github.pagehelper.PageInfo;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.vo.ProductDetailVo;

public interface IProductService {

    /**
     * 后台保存或更新商品
     * @param product 商品
     * @return
     */
    ServerResponse saveOrUpdate(Product product);

    /**
     * 后台设置销售状态
     * @param productId 商品id
     * @param status 状态码
     * @return
     */
    ServerResponse setSaleStatus(Integer productId, Integer status);

    /**
     * 后台获取商品详情
     * @param productId 商品id
     * @return vo对象
     */
    ServerResponse<ProductDetailVo> manageProductDetail(Integer productId);

    /**
     * 后台获取商品列表
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return 返回分页数据
     */
    ServerResponse<PageInfo> getProductList(int pageNum, int pageSize);

    /**
     * 查找商品
     * @param productName 商品名
     * @param productId 商品id
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return 返回分页数据
     */
    ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize);

    /**
     * 前台获取商品详情
     * @param productId 商品id
     * @return vo对象
     */
    ServerResponse<ProductDetailVo> getProductDetail(Integer productId);

    /**
     * 前台查找商品
     * @param keyword 搜索关键字
     * @param categoryId 品类id
     * @param orderBy 排序规则
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @return
     */
    ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, String orderBy,
                                                         int pageNum, int pageSize);
}
