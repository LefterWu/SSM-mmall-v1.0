package com.mmall.service.impl;

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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @Description: 商品品类相关业务
 * @author: wuleshen
 */
@Service("iCategoryService")
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
        //得到子品类List
        List<Category> categoryList = categoryMapper.selectChildrenCategoryByParentId(categoryId);
        if(CollectionUtils.isEmpty(categoryList)) {
            //这里打印一行日志，不用返回错误，不然前端没有内容展示
            logger.info("未找到当前分类的子分类");
        }
        return ServerResponse.createBySuccess(categoryList);
    }


    @Override
    public ServerResponse<List<Integer>> getCategoryAndChildrenById(Integer categoryId) {
        Set<Category> categorySet = new HashSet<>();
        findChildCategory(categorySet, categoryId);

        //用一个List存放被查节点的id
        List<Integer> categoryIdList = new ArrayList<>();
        if (categoryId != null) {
            for(Category categoryItem: categorySet) {
                categoryIdList.add(categoryItem.getId());
            }
        }
        return  ServerResponse.createBySuccess(categoryIdList);
    }


    /**
     * 递归查找子节点
     * @param categorySet 被查找品类及其子品类会被添加到这个Set
     * @param categoryId 品类id
     * @return Category的Set集合
     */
    private Set<Category> findChildCategory(Set<Category> categorySet, Integer categoryId) {
        Category category = categoryMapper.selectByPrimaryKey(categoryId);
        if(category != null) {
            categorySet.add(category);
        }
        //查找下一层的所有子节点
        List<Category> categoryList = categoryMapper.selectChildrenCategoryByParentId(categoryId);
        //调试用
        if(logger.isInfoEnabled()) {
            for(Category c: categoryList) {
                logger.info("categoryList: " + c.toString());
            }
        }
        //如果有子节点，递归调用
        for (Category categoryItem: categoryList) {
            findChildCategory(categorySet, categoryItem.getId());
        }
        //如果没有子节点，既查不到categoryList，mybatis不会返回null，程序执行到这里，直接返回Set
        return categorySet;
    }

}
