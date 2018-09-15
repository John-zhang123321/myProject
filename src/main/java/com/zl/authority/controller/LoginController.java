package com.zl.authority.controller;

import com.zl.authority.Utils.RedisUtil;
import com.zl.authority.constant.ResponseCode;
import com.zl.authority.dao.MySessionDao;
import com.zl.authority.entity.MySession;
import com.zl.authority.entity.ResultObject;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Created by zhangliang on 2018/9/1.
 */
@RestController
public class LoginController {
    @Autowired
    MySessionDao mySessionDao;

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResultObject login(String username,String password){
        ResultObject resultObject = new ResultObject();
        resultObject.setResponseCode(ResponseCode.FAILURE_CODE);

        Subject subject = SecurityUtils.getSubject();
        Session session = subject.getSession();
        String sessionId = session.getId().toString();
        // 判断是否已登录，如果已登录，则回跳，防止重复登录
        String hasCode = RedisUtil.get("zhang_" + sessionId);
        // code校验值
        if (StringUtils.isBlank(hasCode)) {
            // 使用shiro认证
            UsernamePasswordToken usernamePasswordToken = new UsernamePasswordToken(username, password);
            try {
                subject.login(usernamePasswordToken);
            } catch (IncorrectCredentialsException e) {
                resultObject.setMsg("密码错误");
                return resultObject;
            } catch (LockedAccountException e) {
                resultObject.setMsg("登录失败，该用户已被冻结");
                return resultObject;
            } catch (AuthenticationException e) {
                resultObject.setMsg("该用户不存在");
                return resultObject;
            }
            // 更新session状态
            mySessionDao.updateStatus(sessionId, MySession.OnlineStatus.on_line);
            // 全局会话sessionId列表，供会话管理
            RedisUtil.lpush(ZHENG_UPMS_SERVER_SESSION_IDS, sessionId.toString());
            // 默认验证帐号密码正确，创建code
            String code = UUID.randomUUID().toString();
            // 全局会话的code
            RedisUtil.set(ZHENG_UPMS_SERVER_SESSION_ID + "_" + sessionId, code, (int) subject.getSession().getTimeout() / 1000);
            // code校验值
            RedisUtil.set(ZHENG_UPMS_SERVER_CODE + "_" + code, code, (int) subject.getSession().getTimeout() / 1000);

        }

        resultObject.setMsg("验证成功");
        resultObject.setResponseCode(ResponseCode.SUCCESS_CODE);
        return resultObject;
    }
}
