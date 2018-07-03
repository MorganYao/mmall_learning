package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServiceResponse;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @author : pengyao
 * @Date: 2018/6/2 20: 59
 */
@Service("iUserService")//便于在controller中注入
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServiceResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0){
            return ServiceResponse.createByErrorMessage("该用户名不存在");
        }

        //密码登录MD5
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, md5Password);

        if (user == null){
            //逻辑到这说明用户名存在，不然在上一步已经返回消息
            return ServiceResponse.createByErrorMessage("密码错误");
        }

        //用户登陆成功后界面展示用户名，所以把user对象中的密码置为空
        user.setPassword(StringUtils.EMPTY);
        return ServiceResponse.createBySuccess("登陆成功",user);
    }

    public ServiceResponse<String> register(User user){
//        int resultCount = userMapper.checkUsername(user.getUsername());
//        if (resultCount > 0){
//            return ServiceResponse.createByErrorMessage("该用户名已存在");
//        }
//        resultCount = userMapper.checkEmail(user.getEmail());
//        if (resultCount > 0){
//            return ServiceResponse.createByErrorMessage("该邮箱已存在");
//        }
        ServiceResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);
        if(!validResponse.isSuccess()){
            return validResponse;
        }

        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);
        if(!validResponse.isSuccess()){
            return validResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);

        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount == 0){
            return ServiceResponse.createByErrorMessage("注册失败");
        }
        return ServiceResponse.createBySuccessMessge("注册成功");
    }

    public ServiceResponse<String> checkValid(String str, String type){
        if(StringUtils.isNotBlank(type)) {
            //开始校验
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServiceResponse.createByErrorMessage("该用户名已存在");
                }
            }

            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServiceResponse.createByErrorMessage("该邮箱已存在");
                }
            }
        }else{
            return ServiceResponse.createByErrorMessage("输入参数有误");
        }
        return ServiceResponse.createBySuccessMessge("检验成功");
    }

    public ServiceResponse selectQuestion(String username){
        ServiceResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServiceResponse.createByErrorMessage("该用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServiceResponse.createBySuccess(question);
        }
        return ServiceResponse.createByErrorMessage("找回密码的问题不存在");
    }

    public ServiceResponse<String> checkAnswer(String username,String question, String answer){
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if (resultCount > 0){
            //问题是该用户的且答案正确
            String forgetToken = UUID.randomUUID().toString();
            //TokenCache.setKey(TokenCache.TOKEN_PREFIX + username,forgetToken);
            RedisShardedPoolUtil.setEx(Const.TOKEN_PREFIX + username,60 * 60 * 12,forgetToken);
            return ServiceResponse.createBySuccess(forgetToken);
        }
        return ServiceResponse.createByErrorMessage("答案回答错误");
    }

    public ServiceResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if (StringUtils.isBlank(forgetToken)){
            return ServiceResponse.createByErrorMessage("参数错误，token为空，需要传递token值");
        }
        ServiceResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServiceResponse.createByErrorMessage("该用户不存在");
        }

        //String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        String token = RedisShardedPoolUtil.get(Const.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)){
            return ServiceResponse.createByErrorMessage("token无效或已过期");
        }
        if ((StringUtils.equals(forgetToken,token))) {
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePaaswordByUsername(username,md5Password);
            if(rowCount > 0){
                return ServiceResponse.createBySuccessMessge("密码修改成功");
            }
        }else {
            return ServiceResponse.createByErrorMessage("token错误，请重新获取重置密码的token");
        }

        return ServiceResponse.createByErrorMessage("密码修改失败");
    }

    public ServiceResponse<String> resetPassword(String password, String passwordNew, User user){
        //防止横向越权，需要检验旧密码是否是指向该用用户，因为利用count（1）进行查询，如果不指定id，大概率查询结构大于0
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(password),user.getId());
        if(resultCount == 0){
            return ServiceResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount > 0){
            return ServiceResponse.createBySuccessMessge("密码更新成功");
        }
        return ServiceResponse.createByErrorMessage("密码更新失败");
    }

    public ServiceResponse<User> updateInformation(User user){
        //username不能被更新
        //email需要校验，校验新的email是否存在，如果存在，不能是当前用户的
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if (resultCount > 0){
            return ServiceResponse.createByErrorMessage("email已存在，请更换email后再尝试更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if (updateCount > 0){
            return ServiceResponse.createBySuccess("更新个人信息成功", updateUser);
        }
        return ServiceResponse.createByErrorMessage("更新个人信息失败");
    }

    public ServiceResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServiceResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServiceResponse.createBySuccess(user);
    }


    /**
     * 校验用户是否是管理员
     * @param user
     * @return
     */
    public ServiceResponse checkAdminRole(User user){
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServiceResponse.createBySuccess();
        }
        return ServiceResponse.createByError();
    }
}
