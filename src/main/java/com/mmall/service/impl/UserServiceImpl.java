package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
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
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("用户名或密码错误");
        }

        //将返回的用户密码置空
        user.setPassword(StringUtils.EMPTY);
        //返回成功响应
        return ServerResponse.createBySuccess("登录成功", user);
    }

    @Override
    public ServerResponse<String> register(User user) {
        //校验用户名和邮箱
        ServerResponse<String> validResponse = checkValid(user.getUsername(), Const.USERNAME);
        if (!validResponse.isSuccuess()) {
            return validResponse;
        }
        validResponse = checkValid(user.getEmail(), Const.EMAIL);
        if (!validResponse.isSuccuess()) {
            return validResponse;
        }
        //通过校验
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //密码MD5处理
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        //插入用户
        int resultCount = userMapper.insert(user);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 校验指定类型的参数
     * @param str 待校验参数
     * @param type 参数类型（Const类中）
     * @return 校验成功，返回错误响应（xxx已存在/参数错误）；校验失败，返回成功响应
     */
    @Override
    public ServerResponse<String> checkValid(String str, String type) {
        //如果type不为空 ，分别校验用户名和密码
        if (StringUtils.isNotBlank(type)) {
            //如果类型为用户名，则检查该用户名是否存在
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            //如果类型为邮箱，则检查该邮箱是否存在
            if (Const.EMAIL.equals(type)) {
                if (Const.EMAIL.equals(type)) {
                    int resultCount = userMapper.checkEmail(str);
                    if (resultCount > 0) {
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
        //检查username是否存在，复用checkValid方法
        ServerResponse<String> validResponse = checkValid(username, Const.USERNAME);
        //username不存在，则校验成功，因此判断response为成功时，username不存在
        if (validResponse.isSuccuess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("该用户未设置找回密码的问题");
    }

    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int answerCount = userMapper.checkAnswer(username, question, answer);
        if (answerCount > 0) {
            //表明问题答案正确，且属于当前用户
            String forgetToken = UUID.randomUUID().toString();
            //将forgetToken放到本地cache中
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题答案错误");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        //如果token为空，返回错误响应
        if(StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("token为空，请先获取token");
        }
        //校验用户名，如果用户名不存在，返回错误响应
        ServerResponse<String> validResponse = this.checkValid(username, Const.USERNAME);
        if(validResponse.isSuccuess()) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        //校验token
        //从缓存中读取token
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if(StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或过期");
        }
        //通过token校验
        if(StringUtils.equals(token, forgetToken)) {
            //开始重置密码
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);
            if( rowCount > 0 ) {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            //token校验失败
            return ServerResponse.createByErrorMessage("token错误，请重新回答密保问题");
        }
        return ServerResponse.createByErrorMessage("重置密码失败");
    }

    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        //为防止横向越权，需要先校验用户的旧密码，同时指定用户id
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if(resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int rowCount = userMapper.updateByPrimaryKeySelective(user);
        if( rowCount > 0 ) {
            return ServerResponse.createBySuccessMessage("密码修改成功");
        }
        return ServerResponse.createByErrorMessage("密码修改失败");
    }

    @Override
    public ServerResponse<User> updateInformation(User user) {
        //email需要校验,校验新的email是否被其他用户使用
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if(resultCount > 0) {
            return ServerResponse.createByErrorMessage("email已存在，请更换email");
        }
        //updateUser，需要更新的user对象，只更新部分字段
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        resultCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if( resultCount > 0 ) {
            return ServerResponse.createBySuccess("更新个人信息成功", updateUser);
        }
        return ServerResponse.createByErrorMessage("用户更新失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer userId) {

        User user = userMapper.selectByPrimaryKey(userId);
        if( user == null ) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        //将用户密码置空，并放入response返回
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }


}
