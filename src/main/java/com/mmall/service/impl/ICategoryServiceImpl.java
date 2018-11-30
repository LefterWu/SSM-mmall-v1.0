package com.mmall.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.pojo.Category;
import com.mmall.service.ICategoryService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description: 商品品类相关业务
 * @author: wuleshen
 */
@Service
public class ICategoryServiceImpl implements ICategoryService {

    private Logger logger = LoggerFactory.getLogger(ICategoryServiceImpl.class);

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ServerResponse addCategory(String categoryName, Integer parentId) {
        if(StringUtils.isBlank(categoryName) || parentId == null) {
            return ServerResponse.createByErrorMessage("添加品类参数错误");
        }
        Category category = new Category();
        category.setName(categoryName);
        category.setParentId(parentId);
        //设置当前品类为可用的
        category.setStatus(true);
        int insertCount = categoryMapper.insert(category);
        //插入数据成功
        if(insertCount > 0) {
            return ServerResponse.createBySuccessMessage("添加品类成功");
        }
        return ServerResponse.createByErrorMessage("添加品类失败");
    }

    @Override
    public ServerResponse updateCategoryName(String categoryName, Integer categoryId) {
        if(StringUtils.isBlank(categoryName) || categoryId == null) {
            return  ServerResponse.createByErrorMessage("更新品类参数错误");
        }
        Category category = new Category();
        category.setId(categoryId);
        category.setName(categoryName);
        int updateCount = categoryMapper.updateByPrimaryKeySelective(category);
        //修改品类名称成功
        if(updateCount > 0) {
            return ServerResponse.createBySuccessMessage("修改品类名称成功");
        }
        return ServerResponse.createByErrorMessage("修改品类名称失败");
    }

    @Override
    public ServerResponse<List<Category>> getChildrenParallelCategory(Integer categoryId) {
        if(categoryId == null) {
            return  ServerResponse.createByErrorMessage("获得子节点参数错误");
        }
        //得到品类List
        List<Category> categoryList = categoryMapper.selectChildrenCategoryByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)) {
            //这里打印一行日志，不用返回错误，不然前端没有内容展示
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }

    /**
     * 递归查询本节点id及子节点id
     * @param categoryId 品类id
     * @return 本节点及子节点idList
     */
    public ServerResponse<List<Integer>> getCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorySet = Sets.newHashSet();
        findChildCategory(categorySet, categoryId);

        List<Integer> categoryIdList = Lists.newArrayList();
        if (categoryId != null) {
            for(Category categoryItem: categorySet) {
                categoryIdList.add(categoryItem.getId());
            }
        }
        return  ServerResponse.createBySuccess(categoryIdList);
    }


    /**
     * 递归查找子节点
     * @param categorySet 递归调用的中间参数
     * @param categoryId 品类id
     * @return Category的Set集合
     */
    private Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null) {
            categorySet.add(category);
        }
        //查找子节点
        List<Category> categoryList = categoryMapper.selectChildrenCategoryByParentId(categoryId);
        //如果查询子节点不为空，则递归调用
        if(!CollectionUtils.isEmpty(categoryList)) {
            for (Category c: categoryList) {
                findChildCategory(categorySet, c.getId());
            }
        }
        return categorySet;
    }

}
