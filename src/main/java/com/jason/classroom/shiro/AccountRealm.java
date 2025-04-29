package com.jason.classroom.shiro;

import com.jason.classroom.entity.User;
import com.jason.classroom.service.UserService;
import com.jason.classroom.util.JwtUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountRealm extends AuthorizingRealm {

    @Autowired
    JwtUtils jwtUtils;  // 用于处理JWT的工具类
    @Autowired
    UserService userService;  // 用户服务，用于获取用户信息

    /**
     * 判断是否支持该Token
     * 该方法判断是否支持JwtToken作为身份验证的Token
     * @param token 要验证的Token
     * @return 如果是JwtToken则返回true，否则返回false
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;  // 判断是否是JwtToken类型
    }

    /**
     * 获取授权信息
     * 目前该方法未实现授权逻辑，返回null
     * @param principalCollection 身份信息集合
     * @return 返回授权信息
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;  // 目前不实现授权部分
    }

    /**
     * 获取认证信息
     * 该方法处理身份验证逻辑，验证JWT Token的有效性，并返回认证信息
     * @param token 身份验证Token，通常是JwtToken
     * @return 返回认证信息，如果验证通过
     * @throws AuthenticationException 如果身份验证失败，抛出相关异常
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {

        JwtToken jwtToken = (JwtToken) token;  // 将AuthenticationToken转换为JwtToken

        // 从JWT中获取用户ID
        String userId = jwtUtils.getClaimByToken((String) jwtToken.getPrincipal()).getSubject();

        // 根据用户ID查询用户信息
        User user = userService.getById(Integer.valueOf(userId));
        if (user == null){
            // 如果用户不存在，抛出账户不存在异常
            throw new UnknownAccountException("账户不存在");
        }
        // 如果账户已被锁定，抛出账户被锁定异常
        if (user.getStatus() == 2){
            throw new LockedAccountException("账户已被锁定");
        }

        // 将用户信息拷贝到AccountProfile对象中
        AccountProfile profile = new AccountProfile();
        BeanUtils.copyProperties(user, profile);

        // 返回认证信息，包括用户资料、凭证（JWT）、Realm的名称
        return new SimpleAuthenticationInfo(profile, jwtToken.getCredentials(), getName());
    }
}

