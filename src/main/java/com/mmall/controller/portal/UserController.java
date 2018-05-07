package com.mmall.controller.portal;

import com.mmall.common.Const;
import com.mmall.common.ResponseCode;
import com.mmall.common.ServerResponse;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.CookieUtil;
import com.mmall.util.JsonUtil;
import com.mmall.util.RedisShardedPoolUtil;
import com.mmall.util.RedisShardedPoolUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @Author Wuleijian
 * @Date 2018/4/2 11:15
 * @Description
 */

@Controller
@RequestMapping("/user/")
public class UserController {

    @Autowired
    private IUserService iUserService;


    @RequestMapping(value = "login.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> login(String username, String password, HttpSession session, HttpServletResponse httpServletResponse) {
        ServerResponse<User> response = iUserService.login(username, password);
        if (response.isSuccess()) {
//            session.setAttribute(Const.CURRENT_USER, response.getData());
            CookieUtil.writeLoginToken(httpServletResponse, session.getId());
            RedisShardedPoolUtil.setEx(session.getId(), JsonUtil.obj2String(response.getData()), Const.RedisCacheExtime.REDIS_SESSION_EXTIME);

        }
        return response;
    }

    @RequestMapping(value = "logout.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        CookieUtil.delLoginToken(httpServletRequest, httpServletResponse);
        RedisShardedPoolUtil.del(loginToken);
//        session.removeAttribute(Const.CURRENT_USER);
        return ServerResponse.createBySuccess();
    }

    @RequestMapping(value = "register.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user) {
        return iUserService.register(user);
    }

    @RequestMapping(value = "check_valid.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str, String type) {
        return iUserService.checkValid(str, type);
    }

    @RequestMapping(value = "get_user_info.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpServletRequest httpServletRequest) {

//        User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if (user != null) {
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
    }

    @RequestMapping(value = "forget_get_question.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username) {
        return iUserService.selectQuestion(username);
    }

    @RequestMapping(value = "forget_check_answer.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username, String question, String answer) {
        return iUserService.checkAnswer(username, question, answer);
    }

    @RequestMapping(value = "forget_reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetRestPassword(String username, String passwordNew, String forgetToken) {
        return iUserService.forgetResetPassword(username, passwordNew, forgetToken);
    }

    @RequestMapping(value = "reset_password.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpServletRequest httpServletRequest, String passwordOld, String passwordNew) {
//        User user = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (org.apache.commons.lang.StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User user = JsonUtil.string2Obj(userJsonStr, User.class);

        if (user == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld, passwordNew, user);
    }

    @RequestMapping(value = "update_information.do", method = RequestMethod.POST)
    @ResponseBody
    //虽然传入的参数不是完整的user，但也可以用user当参数
    public ServerResponse<User> update_information(HttpServletRequest httpServletRequest, User user) {
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (org.apache.commons.lang.StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(userJsonStr, User.class);

        if (currentUser == null) {
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());
        user.setUsername(currentUser.getUsername());
        ServerResponse<User> response = iUserService.updateInformation(user);
        if (response.isSuccess()) {
            response.getData().setUsername(currentUser.getUsername());
//            session.setAttribute(Const.CURRENT_USER, response.getData());
            RedisShardedPoolUtil.setEx(loginToken, JsonUtil.obj2String(response.getData()), Const.RedisCacheExtime.REDIS_SESSION_EXTIME);
        }
        return response;
    }

    @RequestMapping(value = "get_information.do", method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpServletRequest httpServletRequest) {
//        User currentUser = (User) session.getAttribute(Const.CURRENT_USER);
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);
        if (org.apache.commons.lang.StringUtils.isEmpty(loginToken)) {
            return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
        }
        String userJsonStr = RedisShardedPoolUtil.get(loginToken);
        User currentUser = JsonUtil.string2Obj(userJsonStr, User.class);

        if (currentUser == null) {
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(), "未登录,需要强制登录status=10");
        }
        return iUserService.getInformation(currentUser.getId());
    }
}
// package com.mmall.controller.portal;
//
// import com.mmall.common.Const;
// import com.mmall.common.ServerResponse;
// import com.mmall.dao.UserMapper;
// import com.mmall.pojo.User;
// import com.mmall.service.IUserService;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.stereotype.Controller;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestMethod;
// import org.springframework.web.bind.annotation.ResponseBody;
//
// import javax.servlet.http.HttpSession;
//
// @Controller
// @RequestMapping("/user/")
// public class UserController {
//
//     @Autowired
//     private IUserService iUserService;
//
//     @RequestMapping(value = "login.do", method = RequestMethod.POST)
//
//     //一般在异步获取数据时使用（ajax）
//     //使用此注解后，数据不会再走视图处理器，而是直接将数据写入输入流中，他的效果等于通过response对象输出指定格式的数据
//     //使用 @RequestMapping后，返回值通常解析为跳转路径，加了@ResponseBody之后，直接返回json数据
//     //扩展：@RequestBody接受的是json对象的字符串
//     @ResponseBody
//     public ServerResponse<User> login(String username, String password, HttpSession session) {
//         ServerResponse<User> response = iUserService.login(username, password);
//         //返回的response有三种可能，status两次为1，一次为0
//         if (response.isSuccess()) {
//             session.setAttribute(Const.CURRENT_USER, response.getData());
//         }
//         return response;
//     }
//
//     @RequestMapping(value = "logout.do", method = RequestMethod.POST)
//     @ResponseBody
//     //为什么返回String？
//     public ServerResponse<String> logout(HttpSession session) {
//         session.removeAttribute(Const.CURRENT_USER);
//         //为什么用createBySuccess()？
//         return ServerResponse.createBySuccess();
//     }
//
//     @RequestMapping(value = "register.do", method = RequestMethod.POST)
//     @ResponseBody
//     public ServerResponse register(String username, String password, String email, Integer phone, String question, String answer) {
//         UserMapper.
//     }
//
// }