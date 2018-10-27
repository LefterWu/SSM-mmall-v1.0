package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @Description: UserService实现类
 * @author: wuleshen
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {

    @Autowired
    //自动注入dao层对象
    private UserMapper userMapper;

    @Override
    public ServerResponse login(String username, String password) {
        //如果从数据库中查不到输入的用户名，则返回错误信息
        int count = userMapper.checkUsername(username);
        if (count == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        String md5Password = MD5Util.MD5EncodeUtf8(password);

        //如果查询user结果为空，提示密码错误
        User user = userMapper.selectLogin(username,md5Password);
        if(user == null) {
            return ServerResponse.createByErrorMessage("用户名或密码错误");
        }

        //将返回的用户密码置空
        user.setPassword(StringUtils.EMPTY);
        //返回成功响应
        return ServerResponse.createBySuccess("登录成功",user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        //校验用户名和邮箱
        ServerResponse<String> vaildResponse = checkVaild(user.getUsername(), Const.USERNAME);
        if(!vaildResponse.isSuccuess()) {
            return vaildResponse;
        }
        vaildResponse = checkVaild(user.getEmail(), Const.EMAIL);
        if(!vaildResponse.isSuccuess()) {
            return  vaildResponse;
        }
        //通过校验
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //密码MD5处理
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        //插入用户
        int resultCount = userMapper.insert(user);
        if(resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    @Override
    public ServerResponse<String> checkVaild(String str, String type) {
        //如果type不为空 ，分别校验用户名和密码
        if(StringUtils.isNotBlank(type)) {
            //如果类型为用户名，则检查该用户名是否存在
            if(Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if(resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            //如果类型为邮箱，则检查该邮箱是否存在
            if(Const.EMAIL.equals(type)) {
                if(Const.EMAIL.equals(type)) {
                    int resultCount = userMapper.checkEmail(str);
                    if(resultCount > 0) {
                        return ServerResponse.createByErrorMessage("邮箱已存在");
                    }
                }
            }
        } else {
            //如果type为空，返回错误结果
            return ServerResponse.createByErrorMessage("参数错误");
        }
        //校验成功
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    @Override
    public ServerResponse<String> selectQuestion(String username) {
        //检查username是否存在，复用checkVaild方法
        ServerResponse<String> vaildResponse = checkVaild(username, Const.USERNAME);
        //username不存在，则校验成功，因此判断response为成功时，username不存在
        if(vaildResponse.isSuccuess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank("question")) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int answerCount = userMapper.checkAnswer(username, question, answer);
        if(answerCount > 0) {
            //表明问题答案正确，且属于当前用户
            String forgerToken = UUID.randomUUID().toString();
            //将forgetToken放到本地cache中，然后设置cache的有效期

        }

    }


}
