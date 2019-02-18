package com.mmall.controller.backend;

import com.github.pagehelper.PageInfo;
import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.Product;
import com.mmall.pojo.User;
import com.mmall.service.IFileService;
import com.mmall.service.IProductService;
import com.mmall.service.IUserService;
import com.mmall.util.PropertiesUtil;
import com.mmall.vo.ProductDetailVo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 后台商品管理Controller
 */
@Controller
@RequestMapping("/manage/product/")
public class ProductManageController {

    @Autowired
    private IUserService iUserService;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    /**
     * 后台保存或更新商品信息
     * @param session
     * @param product
     * @return
     */
    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product) {
        //校验用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //校验用户是否为管理员
        if ( iUserService.checkAdminRole(user).isSuccess() ) {
            //业务逻辑
            return iProductService.saveOrUpdate(product);
        } else {
            return ServerResponse.createByErrorMessage("不是管理员用户，无法进行操作");
        }
    }

    /**
     * 后台更新商品的销售状态
     * @param session
     * @param productId
     * @param status
     * @return
     */
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId, Integer status) {
        //校验用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //校验用户是否为管理员
        if ( iUserService.checkAdminRole(user).isSuccess() ) {
            //业务逻辑
            return iProductService.setSaleStatus(productId, status);
        } else {
            return ServerResponse.createByErrorMessage("不是管理员用户，无法进行操作");
        }
    }

    /**
     * 后台获取商品详情
     * @param session
     * @param productId
     * @return
     */
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> getDetail(HttpSession session, Integer productId) {
        //校验用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //校验用户是否为管理员
        if ( iUserService.checkAdminRole(user).isSuccess() ) {
            //业务逻辑
            return iProductService.manageProductDetail(productId);
        } else {
            return ServerResponse.createByErrorMessage("不是管理员用户，无法进行操作");
        }
    }


    /**
     * 后台得到所有商品
     * @param session
     * @param pageNum 页码
     * @param pageSize 每页显示数量
     * @return
     */
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> getList(HttpSession session,
                                            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        //校验用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //校验用户是否为管理员
        if ( iUserService.checkAdminRole(user).isSuccess() ) {
            //业务逻辑，添加动态分页
            return iProductService.getProductList(pageNum, pageSize);
        } else {
            return ServerResponse.createByErrorMessage("不是管理员用户，无法进行操作");
        }
    }

    /**
     * 后台搜索商品
     * @param session
     * @param productName
     * @param productId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse<PageInfo> productSearch(HttpSession session, String productName, Integer productId,
                                            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                                            @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        //校验用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //校验用户是否为管理员
        if ( iUserService.checkAdminRole(user).isSuccess() ) {
            //业务逻辑
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        } else {
            return ServerResponse.createByErrorMessage("不是管理员用户，无法进行操作");
        }
    }

    /**
     * 上传文件
     * @param session session
     * @param file 文件
     * @param request http请求
     * @return 将表示文件的uri名称和url地址封装到map，作为返回值返回
     */
    @RequestMapping("upload.do")
    @ResponseBody
    public ServerResponse<Map> upload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file, HttpServletRequest request) {
        //校验用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //校验用户是否为管理员
        if ( iUserService.checkAdminRole(user).isSuccess() ) {

            //创建上传路径，src/main/webapp/upload
            String path = request.getSession().getServletContext().getRealPath("upload");
            //业务逻辑，将文件上传到ftp服务器
            String targetFileName = iFileService.upload(file, path);
            if (targetFileName != null) {
                //拼出完整url，例 http://img.mmall.com/targetFileName
                String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
                //将uri和url封装到Hashmap，作为返回值返回
                Map fileMap = new HashMap<>();
                fileMap.put("uri", targetFileName);
                fileMap.put("url", url);
                return ServerResponse.createBySuccess(fileMap);
            }
            return ServerResponse.createByErrorMessage("上传失败");
        } else {
            return ServerResponse.createByErrorMessage("不是管理员用户，无法进行操作");
        }
    }

    /**
     * 富文本图片上传
     * @param session session
     * @param file 文件
     * @param request http请求
     * @return 将simditor规定的上传返回的json格式，封装成一个map返回
     * 富文本上传返回的json格式
        {
        "success": true/false,
        "msg": "error message", # optional
        "file_path": "[real file path]"
        }
     */
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file", required = false) MultipartFile file,
                                 HttpServletRequest request, HttpServletResponse response) {

        Map resultMap = new HashMap();
        //校验用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if (user == null) {
            resultMap.put("success", false);
            resultMap.put("msg", "请登录管理员用户");
            return resultMap;
        }
        //校验用户是否为管理员
        if ( iUserService.checkAdminRole(user).isSuccess() ) {

            //创建上传路径，src/main/webapp/upload
            String path = request.getSession().getServletContext().getRealPath("upload");
            //业务逻辑，将文件上传到ftp服务器
            String targetFileName = iFileService.upload(file, path);
            //上传失败
            if (StringUtils.isBlank(targetFileName)) {
                resultMap.put("success", false);
                resultMap.put("msg", "上传失败");
                return resultMap;
            }
            //上传成功
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix") + targetFileName;
            resultMap.put("success", true);
            resultMap.put("msg", "上传成功");
            resultMap.put("file_path", url);
            response.addHeader("Access-Control-Allow_Headers", "X-File-Name");
            return resultMap;

        } else {
            resultMap.put("success", false);
            resultMap.put("msg", "不是管理员用户，无法进行操作");
            return resultMap;
        }
    }
}
