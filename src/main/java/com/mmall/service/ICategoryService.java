package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.Category;

import java.util.List;

/**
 * @Description: TODO
 * @author: wuleshen
 */
public interface ICategoryService {

    /**
     * 添加品类
     * @param categoryName 品类名
     * @param parentId 父节点id
     * @return
     */
    ServerResponse addCategory(String categoryName, Integer parentId);

    /**
     * 更新品类名
     * @param categoryName 品类名
     * @param categoryId 品类id
     * @return
     */
    ServerResponse updateCategoryName(String categoryName, Integer categoryId);

    /**
     * 获取子品类及平行品类
     * @param categoryId
     * @return
     */
    ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId);

    /**
     * 递归查询本节点id及子节点id
     * @param categoryId 品类id
     * @return 本节点及子节点idList
     */
    ServerResponse<List<Integer>> getCategoryAndChildrenById(Integer categoryId);
}
