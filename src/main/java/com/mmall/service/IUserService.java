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

    ServerResponse login(String username, String password);

    ServerResponse<String> register(User user);

    ServerResponse<String> checkVaild(String str, String type);

    ServerResponse<String> selectQuestion(String username);

    ServerResponse<String> checkAnswer(String username, String question, String answer);
}
