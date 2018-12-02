package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.ICategoryService;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @Description: 后台目录管理控制器
 * @author: wuleshen
 */

@Controller
@RequestMapping("/manage/category/")
public class CategoryManageController {

    @Autowired
    private ICategoryService iCategoryService;

    @Autowired
    private IUserService iUserService;

    /**
     * 添加目录
     * @param session 当前session
     * @param categoryName 目录名
     * @param parentId 父节点id，默认为0
     * @return
     */
    @RequestMapping("add_category.do")
    @ResponseBody
    public ServerResponse addCategory(HttpSession session, String categoryName,
                                              @RequestParam(value = "parentId", defaultValue = "0") Integer parentId) {
        //校验用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //校验是否为管理员
        if(iUserService.checkAdminRole(user).isSuccuess()) {
            return iCategoryService.addCategory(categoryName, parentId);
        }
        return ServerResponse.createByErrorMessage("不是管理员用户，无法进行操作");
    }

    /**
     * 设置品类名称
     * @param session 当前session
     * @param categoryName 品类名称
     * @param categoryId 品类id
     * @return
     */
    @RequestMapping("set_category_name.do")
    @ResponseBody
    public ServerResponse setCategoryName(HttpSession session, String categoryName, Integer categoryId) {
        //校验用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //校验是否为管理员
        if(iUserService.checkAdminRole(user).isSuccuess()) {
            //更新品类的名称
            return iCategoryService.updateCategoryName(categoryName, categoryId);
        }
        return ServerResponse.createByErrorMessage("不是管理员用户，无法进行操作");
    }

    /**
     * 得到品类的子节点（同一层级）
     * @param session 当前session
     * @param categoryId 品类节点
     * @return
     */
    @RequestMapping("get_category.do")
    @ResponseBody
    public ServerResponse getChildrenParallelCategory(HttpSession session,
                                                      @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        //校验用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //校验是否为管理员
        if(iUserService.checkAdminRole(user).isSuccuess()) {
            //查询同一级别子节点的category信息
            return iCategoryService.getChildrenParallelCategory(categoryId);
        }
        return ServerResponse.createByErrorMessage("不是管理员用户，无法进行操作");
    }

    /**
     * 得到品类的子节点及深层次节点（递归）
     * @param session 当前session
     * @param categoryId 品类节点
     * @return
     */
    @RequestMapping("get_deep_category.do")
    @ResponseBody
    public ServerResponse getCategoryAndDeepChildrenCategory(HttpSession session,
                                                      @RequestParam(value = "categoryId", defaultValue = "0") Integer categoryId) {
        //校验用户是否登录
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "用户未登录，请先登录");
        }
        //校验是否为管理员
        if(iUserService.checkAdminRole(user).isSuccuess()) {
            //查询当前节点和递归子节点的category信息
            return iCategoryService.getCategoryAndChildrenById(categoryId);
        }
        return ServerResponse.createByErrorMessage("不是管理员用户，无法进行操作");
    }

}
