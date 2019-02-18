package com.mmall.controller.backend;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

/**
 * @Description: 后台用户管理控制器
 * @author: wuleshen
 */

@Controller
@RequestMapping("/manage/user/")
public class UserManageController {

    @Autowired
    private IUserService iUserService;

    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        //复用前台登录功能
        ServerResponse response = iUserService.login(username, password);
        //登录成功
        if(response.isSuccess()) {
            //如果登录用户为管理员，
            User user = (User) response.getData();
            if(user.getRole().equals(Const.Role.ROLE_ADMIN)) {
                session.setAttribute(Const.CURRENT_USER, user);
                return response;
            } else {
                return ServerResponse.createByErrorMessage("不是管理员账号，无法登录");
            }
        }
        //登录失败，直接返回response
        return response;
    }
}
