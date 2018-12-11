package com.mmall.service;

import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;


/**
 * @Description: UserService接口
 * @author: wuleshen
 */
public interface IUserService {

    /**
     * 登录
     * @param username 用户名
     * @param password 密码
     * @return
     */
    ServerResponse login(String username, String password);

    /**
     * 注册
     * @param user 用户
     * @return
     */
    ServerResponse<String> register(User user);

    /**
     * 校验指定类型的参数
     * @param str 待校验参数
     * @param type 参数类型（Const类中）
     * @return 校验成功，返回错误响应（xxx已存在/参数错误）；校验失败，返回成功响应
     */
    ServerResponse<String> checkValid(String str, String type);

    /**
     * 获取忘记密码的问题
     * @param username 用户名
     * @return
     */
    ServerResponse<String> selectQuestion(String username);

    /**
     * 检查忘记密码问题的答案
     * @param username 用户名
     * @param question 问题
     * @param answer 答案
     * @return
     */
    ServerResponse<String> checkAnswer(String username, String question, String answer);

    /**
     * 重置密码（忘记密码状态）
     * @param username 用户名
     * @param passwordNew 新密码
     * @param forgetToken 忘记密码的token
     * @return
     */
    ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken);

    /**
     * 重置密码（登录状态）
     * @param passwordOld 旧密码
     * @param passwordNew 新密码
     * @param user 用户
     * @return
     */
    ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user);

    /**
     * 更新用户信息
     * @param user 用户
     * @return
     */
    ServerResponse<User> updateInformation(User user);

    /**
     * 获取用户信息
     * @param userId 用户id
     * @return
     */
    ServerResponse<User> getInformation(Integer userId);

    /**
     * 校验用户是否为管理员
     * @param user 用户
     * @return
     */
    ServerResponse checkAdminRole(User user);
}
