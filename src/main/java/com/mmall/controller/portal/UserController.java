package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import org.apache.commons.lang.StringUtils;
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
    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
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
    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        return iUserService.checkValid(str, type);
    }

    /**
     * 取得用户信息
     * @param session 当前的session
     * @return 用户不存在时，返回错误响应；用户存在时，返回成功响应
     */
    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session) {
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户信息");
        }
        return ServerResponse.createBySuccess(user);
    }

    /**
     * 得到忘记密码的问题
     * @param username 用户名
     * @return 如果用户不存在或问题为空，则返回错误响应；否则，返回成功响应
     */
    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
        return iUserService.selectQuestion(username);
    }

    /**
     * 检查忘记密码的答案
     * @param username
     * @param question
     * @param answer
     * @return 如果答案为空或者错误，返回错误响应；否则，返回成功响应
     */
    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        return iUserService.checkAnswer(username, question, answer);
    }

    /**
     * 重置密码（忘记密码状态下）
     * @param username
     * @param passwordNew
     * @param forgetToken
     * @return 如果token过期或者token错误，返回错误响应；token校验成功，并且密码更新成功，返回成功响应
     */
    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    /**
     * 重置密码（登录状态下）
     * @param session
     * @param passwordOld
     * @param passwordNew
     * @return 旧密码错误或更新失败，返回错误响应；更新成功，返回成功响应
     */
    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session, String passwordOld, String passwordNew) {
        //校验用户名是否存在
        User user = (User) session.getAttribute(Const.CURRENT_USER);
        if(user == null) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        return iUserService.resetPassword(passwordOld, passwordNew, user);
    }

    /**
     * 更新用户信息
     * @param session 当前的session
     * @param user 前台获取的用户输入需要更新的user对象
     * @return
     */
    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> updateInformation(HttpSession session, User user) {
        //校验用户名是否存在
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        //需要将当前user的id设置给更新user
        user.setId(currentUser.getId());
        //调用Service更新用户信息
        ServerResponse<User> serverResponse = iUserService.updateInformation(user);
        //更新成功，serverResponse中的user对象（更新后的user）放到当前session中
        if( serverResponse.isSuccuess() ) {
            serverResponse.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Const.CURRENT_USER, serverResponse.getData());
        }
        return serverResponse;
    }

    /**
     * 获取用户详细信息
     * @param session 当前session
     * @return
     */
    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getInformation(HttpSession session) {
        //校验用户名是否存在
        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        if(currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录，需要强制登录，code=10");
        }
        return iUserService.getInformation(currentUser.getId());
    }
}
