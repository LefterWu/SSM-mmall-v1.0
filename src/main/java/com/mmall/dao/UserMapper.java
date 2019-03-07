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
     * @return 忘记密码的问题
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

    /**
     * 更新用户密码
     * @param username
     * @param passwordNew
     * @return 更新条数，大于0即为更新成功
     */
    int updatePasswordByUsername(@Param("username") String username, @Param("passwordNew") String passwordNew);

    /**
     * 校验用户密码
     * @param passwordOld
     * @param userId
     * @return 查询结果数量,大于0表示旧密码正确
     */
    int checkPassword(@Param("passwordOld") String passwordOld, @Param("userId") int userId);

    /**
     * 校验用户邮箱
     * @param email 被检查的email
     * @param userId 用户id
     * @return 查询结果数量，大于0表示，有其他用户占用了当前输入的email
     */
    int checkEmailByUserId(@Param("email") String email, @Param("userId") Integer userId);
}