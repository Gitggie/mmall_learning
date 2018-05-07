package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.RedisPool;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import com.mmall.util.RedisShardedPoolUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * @Author Wuleijian
 * @Date 2018/4/2 11:27
 * @Description
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        //todo 密码登录MD5
        String Md5Password = MD5Util.MD5EncodeUtf8(password);
        User user = userMapper.selectLogin(username, Md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);

    }

    public ServerResponse<String> register(User user) {
        // int resultCount = userMapper.checkUsername(user.getUsername());
        // if (resultCount > 0) {
        //     return ServerResponse.createByErrorMessage("用户名已存在");
        // }
        //this表示什么？就是我要用这个实现类本身的方法，不写this也没报红
        ServerResponse validResponse = this.checkValid(user.getUsername(), Const.USERNAME);
        //.优先级比！高
        // 如果validResponse.isSuccess()是false，即validResponse返回error信息，返回validResponse
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        validResponse = this.checkValid(user.getEmail(), Const.EMAIL);
        if (!validResponse.isSuccess()) {
            return validResponse;
        }
        // resultCount = userMapper.checkEmail(user.getEmail());
        // if (resultCount > 0) {
        //     return ServerResponse.createByErrorMessage("Email已存在");
        // }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        //insert没有返回值啊，默认resultCount就是0，下面的判断什么意思？
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    public ServerResponse<String> checkValid(String str, String type) {
        //todo isNotBlank用法
        //先看type是否为空，即检查const类里的数是否正常，因为type放的是const里面的常数USERNAME或EMAIL
        //然后弄两个分支分别校验username和email
        if (org.apache.commons.lang3.StringUtils.isNotBlank(type)) {
            //开始校验
            if (Const.USERNAME.equals(type)) {
                int resultCount = userMapper.checkUsername(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int resultCount = userMapper.checkEmail(str);
                if (resultCount > 0) {
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        //无论如何都会执行？哇你这么能这么理解，return就代表结束了
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    //为什么这里又不写<String>?
    public ServerResponse selectQuestion(String username) {
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (org.apache.commons.lang3.StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username, question, answer);
        if (resultCount > 0) {
            //说明问题及问题答案是这个用户的,并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            //TokenCache里有localCache本地缓存，存储键值对，为什么这么做呢？
//            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            RedisShardedPoolUtil.setEx(Const.TOKEN_PREFIX + username, forgetToken, 60 * 60 * 12);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if (org.apache.commons.lang3.StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误,token需要传递");
        }
        ServerResponse validResponse = this.checkValid(username, Const.USERNAME);
        if (validResponse.isSuccess()) {
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
//        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        String token = RedisShardedPoolUtil.get(Const.TOKEN_PREFIX + username);

        if (org.apache.commons.lang3.StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }
        if (org.apache.commons.lang3.StringUtils.equals(forgetToken, token)) {
            String md5Password = MD5Util.MD5EncodeUtf8(passwordNew);
            //update没有resultType啊，要说有也是int啊，为什么rowCount等于1
            //在使用 mybatis 做持久层时，insert、update、delete，sql 语句默认是不返回被操作记录主键的，而是返回被操作记录条数；
            int rowCount = userMapper.updatePasswordByUsername(username, md5Password);

            if (rowCount > 0) {
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
        }
        //todo 下面这句话好像用不到吧！？哎呀，是rowCount小于0才用到
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    //测试的时候没有传id，拜托你测的在controller里
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0;
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld), user.getId());
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        //todo sql语句不理解：!=null，可你传的user都是非空啊，这样的update不就没有意义了么？
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if (updateCount > 0) {
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    public ServerResponse<User> updateInformation(User user) {
        //username是不能被更新的
        //email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (resultCount > 0) {
            return ServerResponse.createByErrorMessage("email已存在,请更换email再尝试更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        //我两次都更新一样的信息，为什么updateCount还是1
        if (updateCount > 0) {
            return ServerResponse.createBySuccess("更新个人信息成功", updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }


    //backend

    /**
     * 校验是否是管理员
     *
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user) {
        //intValue（）：integer的方法，以 int 类型返回该 Integer 的值
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
}


// package com.mmall.service.impl;
//
// import com.mmall.common.ServerResponse;
// import com.mmall.dao.UserMapper;
// import com.mmall.pojo.User;
// import com.mmall.service.IUserService;
// import com.mmall.util.MD5Util;
// import org.apache.commons.lang3.StringUtils;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Service;
//
// @Service("iUserService")
// public class UserServiceImpl implements IUserService {
//
//
//     @Autowired
//     private UserMapper userMapper;
//
//     //这里没写@override
//     //指定返回ServerResponse类，<>里面放User类
//     public ServerResponse<User> login(String username, String password) {
//         //判断用户是否存在
//         int resultCount = userMapper.checkUsername(username);
//         if (resultCount == 0) {
//             return ServerResponse.createByErrorMessage("用户名不存在");
//         }
//         //用户名存在则判断密码对错
//         String Md5Password = MD5Util.MD5EncodeUtf8(password);
//         User user = userMapper.selectLogin(username, Md5Password);
//         if (user == null) {
//             return ServerResponse.createByErrorMessage("密码错误");
//         }
//         //为什么还要把密码置空？因为之后你还要在前端返回user，密码不能显示出来
//         //todo StringUtil的用法，其中EMPTY = ""，是null吗？
//         //省略了org.apache.commons.lang3
//         user.setPassword(StringUtils.EMPTY);
//         return ServerResponse.createBySuccess("登录成功", user);
//     }
//
//     public ServerResponse register(User user) {
//         ServerResponse validResponse = this.checkValid
//     }
//
//     public ServerResponse checkValid(User user) {
//
//     }
//
// }