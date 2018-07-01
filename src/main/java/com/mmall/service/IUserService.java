package com.mmall.service;

import com.mmall.common.ServiceResponse;
import com.mmall.pojo.User;

/**
 * @author : pengyao
 * @Date: 2018/6/2 20: 53
 */
public interface IUserService {
    ServiceResponse<User> login(String username, String password);
    ServiceResponse<String> register(User user);
    ServiceResponse<String> checkValid(String str, String type);
    ServiceResponse selectQuestion(String username);
    ServiceResponse<String> checkAnswer(String username,String question, String answer);
    ServiceResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken);
    ServiceResponse<String> resetPassword(String password, String passwordNew, User user);
    ServiceResponse<User> updateInformation(User user);
    ServiceResponse<User> getInformation(Integer userId);

    ServiceResponse checkAdminRole(User user);
}
