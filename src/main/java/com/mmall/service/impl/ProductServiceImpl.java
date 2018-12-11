package com.mmall.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.dao.CategoryMapper;
import com.mmall.dao.ProductMapper;
import com.mmall.pojo.Category;
import com.mmall.pojo.Product;
import com.mmall.service.ICategoryService;
import com.mmall.service.IProductService;
import com.mmall.util.DateTimeUtil;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import com.mmall.vo.ProductListVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    //后端业务

    @Override
    public ServerResponse saveOrUpdate(Product product) {
        if (product != null) {
            //将子图的第一张保存为主图
            if (StringUtils.isNotBlank(product.getSubImages())) {
                String[] subImageArray = product.getSubImages().split(",");
                if (subImageArray.length > 0) {
                    product.setMainImage(subImageArray[0]);
                }
            }
            if (product.getId() != null) {
                //商品id不为空更新操作
                int updateRow = productMapper.updateByPrimaryKey(product);
                if (updateRow > 0) {
                    return ServerResponse.createBySuccessMessage("更新商品成功");
                } else {
                    return ServerResponse.createByErrorMessage("更新商品失败");
                }
            } else {
                //保存操作
                int insertRow = productMapper.insert(product);
                if (insertRow > 0) {
                    return ServerResponse.createBySuccessMessage("更新商品成功");
                } else {
                    return ServerResponse.createByErrorMessage("更新商品失败");
                }
            }
        }
        return ServerResponse.createByErrorMessage("新增或更新产品参数错误");
    }

    @Override
    public ServerResponse setSaleStatus(Integer productId, Integer status) {
        //参数错误，返回响应信息
        if (productId == null || status == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();
        product.setId(productId);
        product.setStatus(status);
        int updateRow = productMapper.updateByPrimaryKeySelective(product);
        if (updateRow > 0) {
            return ServerResponse.createBySuccessMessage("修改商品销售状态成功");
        }
        return ServerResponse.createByErrorMessage("修改商品销售状态失败");
    }

    @Override
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId) {
        //参数错误，返回响应信息
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("商品已下架或删除");
        }
        //简单业务，直接用VO组装页面对象
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);

        return ServerResponse.createBySuccess(productDetailVo);

    }

    /**
     * 装配商品详情VO对象
     * @param product
     * @return
     */
    private ProductDetailVo assembleProductDetailVo(Product product) {
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setName(product.getName());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setStock(product.getStock());
        productDetailVo.setStatus(product.getStatus());

        //imageHost
        productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.mmall.com/"));

        //parentCategoryId
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            productDetailVo.setParentCategoryId(0);     //默认根节点
        }else{
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        //createTime,updateTime
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));

        return productDetailVo;
    }

    @Override
    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize) {
        //开始分页，pageNum 当前页，pageSize 每页显示数量
        PageHelper.startPage(pageNum, pageSize);
        //得到所有产品列表
        //将结果封装成vo对象
        List<Product> productList = productMapper.selectList();
        List<ProductListVo> productListVoList = new ArrayList<>();
        for(Product product: productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        //将结果交给PageInfo处理,先用productList进行分页，然后在把前端要展示的volist设置上
        PageInfo page = new PageInfo(productList);
        page.setList(productListVoList);
        //把分页结果返回
        return ServerResponse.createBySuccess(page);
    }

    @Override
    public ServerResponse<PageInfo> searchProduct(String productName, Integer productId, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum ,pageSize);
        if (StringUtils.isNotBlank(productName)) {
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        List<Product> productList = productMapper.selectByNameAndProductId(productName, productId);

        List<ProductListVo> productListVoList = new ArrayList<>();
        for (Product product: productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }
        //将结果交给PageInfo处理,先用productList进行分页，然后在把前端要展示的volist设置上
        PageInfo page = new PageInfo(productList);
        page.setList(productListVoList);
        return ServerResponse.createBySuccess(page);
    }

    private ProductListVo assembleProductListVo(Product product){
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }


    //前端业务

    @Override
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId) {
        //参数错误，返回响应信息
        if (productId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if (product == null) {
            return ServerResponse.createByErrorMessage("商品已下架或删除");
        }
        if (product.getStatus() != Const.SALE_STATUS.ON_SALE) {
            return ServerResponse.createByErrorMessage("商品已下架或售完");
        }
        //简单业务，直接用VO组装页面对象
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);

        return ServerResponse.createBySuccess(productDetailVo);

    }

    @Override
    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword, Integer categoryId, String orderBy,
                                                                int pageNum, int pageSize) {
        //参数错误处理
        if (StringUtils.isBlank(keyword) && categoryId == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        //得到分类idlist
        List<Integer> categoryIdList = new ArrayList<>();
        if (categoryId != null) {
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            //没有找到分类并且关键字为空,返回一个空的结果
            if (category == null) {
                if (StringUtils.isBlank(keyword)) {
                    PageHelper.startPage(pageNum, pageSize);
                    List<ProductListVo> productListVoList = new ArrayList<>();
                    PageInfo page = new PageInfo(productListVoList);
                    return ServerResponse.createBySuccess(page);
                } else {
                    // 如果没有找到该分类，还有关键字输入，则返回错误响应
                    return ServerResponse.createByErrorMessage("参数错误");
                }
            }
            //找到分类，递归遍历所有子分类
            categoryIdList = iCategoryService.getCategoryAndChildrenById(category.getId()).getData();
        }
        //关键字处理
        if (StringUtils.isNotBlank(keyword)) {
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();
        }

        //排序处理
        //排序前先开启分页
        PageHelper.startPage(pageNum,pageSize);
        if (StringUtils.isNotBlank(orderBy)) {
            if (Const.ORDER_BY.PRICE_ORDER.contains(orderBy)) {
                //常量类里，采用"price_desc"的格式，用_分隔后，重新拼接成sql语句中的格式SELECT ... order by price desc
                String[] orderByArr = orderBy.split("_");
                orderBy = orderByArr[0] + " " + orderByArr[1];
                PageHelper.orderBy(orderBy);
            }
        }
        //调用mapper，在categoryidlist中对关键字模糊查询
        //这里需要对参数进行判断
        List<Product> productList = productMapper.selectByKeywordCategoryIds(
                StringUtils.isBlank(keyword)?null:keyword, categoryIdList.size()==0?null:categoryIdList);
        List<ProductListVo> productListVoList = new ArrayList<>();
        for (Product product: productList) {
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }

        PageInfo page = new PageInfo(productList);
        page.setList(productListVoList);
        return ServerResponse.createBySuccess(page);
    }
}
