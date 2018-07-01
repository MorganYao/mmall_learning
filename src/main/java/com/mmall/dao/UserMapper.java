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

    //检查用户名
    int checkUsername(String username);

    //检查邮箱
    int checkEmail(String email);

    //登陆后返回用户信息
    User selectLogin(@Param("username") String username,@Param("password") String password);

    //查询问题
    String selectQuestionByUsername(String username);

    //检查回答的问题
    int checkAnswer(@Param("username") String username,@Param("question") String question,@Param("answer") String answer);

    //根据用户名更新密码
    int updatePaaswordByUsername(@Param("username") String username,@Param("passwordNew") String passwordNew);

    //检查密码
    int checkPassword(@Param("password") String password,@Param("userId") Integer userId);

    //校验邮箱
    int checkEmailByUserId(@Param("email") String email,@Param("userId") Integer userId);
}