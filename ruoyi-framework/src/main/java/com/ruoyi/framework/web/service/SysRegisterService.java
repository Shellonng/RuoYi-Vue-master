package com.ruoyi.framework.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.RegisterBody;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.user.CaptchaException;
import com.ruoyi.common.exception.user.CaptchaExpireException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.system.service.IUserSyncService;
import com.ruoyi.system.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ruoyi.system.domain.SysUserRole;
import com.ruoyi.system.mapper.SysUserRoleMapper;
import java.util.ArrayList;
import java.util.List;

/**
 * 注册校验方法
 * 
 * @author ruoyi
 */
@Component
public class SysRegisterService
{
    private static final Logger log = LoggerFactory.getLogger(SysRegisterService.class);

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private IUserSyncService userSyncService;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    /**
     * 注册
     */
    public String register(RegisterBody registerBody)
    {
        String msg = "", username = registerBody.getUsername(), password = registerBody.getPassword();
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);

        // 验证码开关
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        if (captchaEnabled)
        {
            validateCaptcha(username, registerBody.getCode(), registerBody.getUuid());
        }

        if (StringUtils.isEmpty(username))
        {
            msg = "用户名不能为空";
        }
        else if (StringUtils.isEmpty(password))
        {
            msg = "用户密码不能为空";
        }
        else if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH)
        {
            msg = "账户长度必须在2到20个字符之间";
        }
        else if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH)
        {
            msg = "密码长度必须在5到20个字符之间";
        }
        else if (!userService.checkUserNameUnique(sysUser))
        {
            msg = "保存用户'" + username + "'失败，注册账号已存在";
        }
        else
        {
            sysUser.setNickName(username);
            sysUser.setPwdUpdateDate(DateUtils.getNowDate());
            sysUser.setPassword(SecurityUtils.encryptPassword(password));
            boolean regFlag = userService.registerUser(sysUser);
            if (!regFlag)
            {
                msg = "注册失败,请联系系统管理人员";
            }
            else
            {
                // ================== 【分配默认角色】 ==================
                try {
                    // 设置默认角色ID (角色ID=100，拥有所有权限)
                    Long roleId = 100L;
                    
                    // 创建用户角色关联
                    SysUserRole ur = new SysUserRole();
                    ur.setUserId(sysUser.getUserId());
                    ur.setRoleId(roleId);
                    
                    // 批量插入
                    List<SysUserRole> list = new ArrayList<>();
                    list.add(ur);
                    sysUserRoleMapper.batchUserRole(list);
                    
                    log.info("用户注册成功并分配默认角色: username={}, userId={}, roleId={}", username, sysUser.getUserId(), roleId);
                } catch (Exception e) {
                    log.error("分配默认角色失败: username={}, error={}", username, e.getMessage(), e);
                }
                // ================== 【分配默认角色结束】 ==================

                // 记录登录日志
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.REGISTER, MessageUtils.message("user.register.success")));

                // 验证同步状态
                try {
                    boolean syncSuccess = verifySyncStatus(sysUser.getUserId(), username);
                    if (!syncSuccess) {
                        log.warn("用户注册成功但同步验证失败: username={}", username);
                    }
                } catch (Exception e) {
                    log.error("验证同步状态失败: username={}, error={}", username, e.getMessage());
                }
            }
        }
        return msg;
    }

    /**
     * 验证用户同步状态
     *
     * @param sysUserId sys_user表的user_id
     * @param username 用户名
     * @return 是否同步成功
     */
    public boolean verifySyncStatus(Long sysUserId, String username)
    {
        try {
            // 等待一小段时间，确保触发器执行完成
            Thread.sleep(500);

            // 查询user表中是否存在对应记录
            User user = userSyncService.getUserBySysUserId(sysUserId);

            if (user == null) {
                log.error("同步验证失败: user表中未找到记录, sysUserId={}, username={}", sysUserId, username);
                return false;
            }

            // 验证关键字段是否一致
            if (!username.equals(user.getUsername())) {
                log.error("同步验证失败: 用户名不一致, expected={}, actual={}", username, user.getUsername());
                return false;
            }

            if (user.getSysUserId() == null || !user.getSysUserId().equals(sysUserId)) {
                log.error("同步验证失败: sys_user_id不一致, expected={}, actual={}", sysUserId, user.getSysUserId());
                return false;
            }

            log.info("同步验证成功: sysUserId={}, username={}, userTableId={}", sysUserId, username, user.getId());
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("同步验证被中断: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("同步验证异常: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 校验验证码
     *
     * @param username 用户名
     * @param code 验证码
     * @param uuid 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String uuid)
    {
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(uuid, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        redisCache.deleteObject(verifyKey);
        if (captcha == null)
        {
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha))
        {
            throw new CaptchaException();
        }
    }
}

