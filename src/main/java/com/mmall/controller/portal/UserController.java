package com.mmall.controller.portal;

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

import static com.mmall.common.ServerResponse.createBySuccess;

/**
 * @Description: 用户控制器，用于用户登录
 * @author: wuleshen
 */
@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;

    /**
     * 用户登录
     * @param username 用户名
     * @param password 密码
     * @return 当登录失败时，返回相应的错误响应；登录成功时，返回成功响应
     */
    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session) {
        ServerResponse response = iUserService.login(username,password);
        //如果响应成功,将user对象保存到session
        if(response.isSuccuess()) {
            session.setAttribute(Const.CURRENT_USER, response.getData());
        }
        return response;
    }

    /**
     * 用户登出
     * @param session 当前会话
     * @return 返回成功响应
     */
    @RequestMapping(value = "logout.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session) {
        session.removeAttribute(Const.CURRENT_USER);
        return createBySuccess();
    }

    /**
     * 用户注册
     * @param user 要注册的用户
     * @return 当注册失败时，返回相应的错误响应；注册成功时，返回成功响应
     */
    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }

    /**
     * 校验用户输入的参数是否已经在数据库中存在
     * @param str 用户输入的参数
     * @param type 参数类型（username，email）
     * @return 如str存在，返回错误响应；如成功，返回成功响应
     */
    @RequestMapping(value = "check_vaild.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> checkVaild(String str, String type) {
        return iUserService.checkVaild(str, type);
    }

    /**
     * 取得用户信息
     * @param session 当前的session
     * @return 用户不存在时，返回错误响应；用户存在时，返回成功响应
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 得到忘记密码的问题
     * @param username 用户名
     * @return 如果用户不存在或问题为空，则返回错误响应；否则，返回成功响应
     */
    @RequestMapping(value = "forfet_get_question.do", method = RequestMethod.GET)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
        return  iUserService.selectQuestion(username);
    }

    public ServerResponse<String> forgetCheckAnswer(String username, String password, String answer) {

    }
}
