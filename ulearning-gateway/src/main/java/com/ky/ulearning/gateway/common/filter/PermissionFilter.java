package com.ky.ulearning.gateway.common.filter;

import com.ky.ulearning.common.core.constant.MicroConstant;
import com.ky.ulearning.common.core.utils.UrlUtil;
import com.ky.ulearning.gateway.common.constant.GatewayConfigParameters;
import com.ky.ulearning.gateway.common.constant.GatewayErrorCodeEnum;
import com.ky.ulearning.gateway.common.exception.JwtTokenException;
import com.ky.ulearning.gateway.common.security.JwtAuthenticationFailureHandler;
import com.ky.ulearning.gateway.common.utils.JwtAccountUtil;
import com.ky.ulearning.spi.system.entity.PermissionEntity;
import com.ky.ulearning.spi.system.entity.RoleEntity;
import io.jsonwebtoken.lang.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 权限过滤器
 * 过滤所有请求
 *
 * @author luyuhao
 * @since 20/01/23 23:20
 */
@Slf4j
@Component
public class PermissionFilter extends OncePerRequestFilter {

    @Autowired
    private GatewayConfigParameters gatewayConfigParameters;

    private final JwtAuthenticationFailureHandler jwtAuthenticationFailureHandler;

    public PermissionFilter(JwtAuthenticationFailureHandler jwtAuthenticationFailureHandler) {
        this.jwtAuthenticationFailureHandler = jwtAuthenticationFailureHandler;
    }

    /**
     * 是否需要过滤
     */
    private boolean shouldFilter(String uri) {
        //访问未放行path时执行
        return UrlUtil.matchUri(uri, gatewayConfigParameters.getPermissionReleasePatterns())
                || UrlUtil.matchUri(uri, gatewayConfigParameters.getAuthenticateReleasePatterns());
    }

    @SuppressWarnings("NullableProblems")
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (shouldFilter(uri)) {
            chain.doFilter(request, response);
            return;
        }

        try {
            if (UrlUtil.matchUri(uri, gatewayConfigParameters.getAdminPatterns())) {
                //访问后台端服务
                adminPermissionCheck(uri);
            } else if (UrlUtil.matchUri(uri, gatewayConfigParameters.getTeacherPatterns())) {
                //访问教师端服务
                teacherPermissionCheck(uri);
            } else if (UrlUtil.matchUri(uri, gatewayConfigParameters.getStudentPatterns())) {
                //访问学生端服务
                studentPermissionCheck(uri);
            } else {
                //网关访问
                gatewayPermissionCheck(uri);
            }
        } catch (AuthenticationException ae) {
            //交给自定义的AuthenticationFailureHandler
            jwtAuthenticationFailureHandler.onAuthenticationFailure(request, response, ae);
            return;
        }
        chain.doFilter(request, response);
    }

    private void gatewayPermissionCheck(String uri) {
        List<PermissionEntity> permissions = JwtAccountUtil.getPermissions();
        //3. 有访问该uri的权限
        if (permissions.stream()
                .map(PermissionEntity::getPermissionUrl)
                .collect(Collectors.toList())
                .stream().noneMatch(s -> UrlUtil.matchUri(uri, s))) {
            warnInfo();
        }
    }

    /**
     * 学生端服务权限校验
     */
    private void studentPermissionCheck(String uri) {
        String sysRole = JwtAccountUtil.getSysRole();
        if (StringUtils.isEmpty(sysRole)
                || !sysRole.equals(MicroConstant.SYS_ROLE_STUDENT)) {
            warnInfo();
        }
    }

    /**
     * 教师端服务权限校验
     */
    private void teacherPermissionCheck(String uri) {
        String sysRole = JwtAccountUtil.getSysRole();
        if (StringUtils.isEmpty(sysRole)
                || !sysRole.equals(MicroConstant.SYS_ROLE_TEACHER)) {
            warnInfo();
        }
    }

    /**
     * 后台端服务权限校验
     */
    private void adminPermissionCheck(String uri) {
        String sysRole = JwtAccountUtil.getSysRole();
        List<RoleEntity> roles = JwtAccountUtil.getRoles();
        List<PermissionEntity> permissions = JwtAccountUtil.getPermissions();
        //1. 用户系统角色为教师; 有教师角色; 有权限
        if (StringUtils.isEmpty(sysRole)
                || !sysRole.equals(MicroConstant.SYS_ROLE_TEACHER)
                || Collections.isEmpty(roles)
                || Collections.isEmpty(permissions)) {
            warnInfo();
            return;
        }
        //2. 教师角色中有管理员角色
        if (!roles.stream()
                .map(RoleEntity::getIsAdmin)
                .collect(Collectors.toList())
                .contains(true)) {
            warnInfo();
            return;
        }
        //3. 有访问该uri的权限
        if (permissions.stream()
                .map(PermissionEntity::getPermissionUrl)
                .collect(Collectors.toList())
                .stream().noneMatch(s -> UrlUtil.matchUri(uri, s))) {
            warnInfo();
        }
    }

    /**
     * 无权限时进行response处理，给予提示
     */
    private void warnInfo() {
        throw new JwtTokenException(GatewayErrorCodeEnum.INSUFFICIENT_PERMISSION.getMessage());
    }
}