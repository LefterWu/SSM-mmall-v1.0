package com.mmall.dao;

import com.mmall.pojo.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    /**
     * 检查用户名是否存在
     * @param username 用户名
     * @return 查询到的用户数量
     */
    int checkUsername(String username);

    /**
     * 查询登录的用户
     * @param username 用户名
     * @param password 密码
     * @return 登录的用户对象
     */
    User selectLogin(@Param("username") String username, @Param("password") String password);

    /**
     * 检查邮箱是否存在
     * @param email 邮箱
     * @return 查询到的邮箱数量
     */
    int checkEmail(String email);

    /**
     * 查询忘记密码的问题
     * @param username 用户名
     * @return 问题
     */
    String selectQuestionByUsername(String username);

    /**
     * 查询指定用户符合问题答案的数量
     * @param username
     * @param question
     * @param answer
     * @return 问题答案的数量，大于0表示问题和答案相符
     */
    int checkAnswer(@Param("username") String username, @Param("question") String question, @Param("answer") String answer);
}