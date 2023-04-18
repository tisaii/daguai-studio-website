package com.tisai.daguai.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tisai.daguai.common.CustomException;
import com.tisai.daguai.common.SystemException;
import com.tisai.daguai.domain.Admin;
import com.tisai.daguai.dto.AdminDto;
import com.tisai.daguai.dto.AdminTokenDto;
import com.tisai.daguai.dto.Result;
import com.tisai.daguai.mapper.AdminMapper;
import com.tisai.daguai.service.AdminService;
import com.tisai.daguai.utils.AdminHolder;
import com.tisai.daguai.utils.SMSUtils;
import com.tisai.daguai.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static com.tisai.daguai.utils.RedisContents.LOGIN_ADMIN_TOKEN_KEY;
import static com.tisai.daguai.utils.RedisContents.VALIDATE_ADMIN_RESET;
import static com.tisai.daguai.utils.SystemContents.REQUEST_AUTHORIZATION_HEADER;

/**
 *
 */
@Service
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin>
    implements AdminService{

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result login(Admin admin) {
//        log.info(admin.toString());
        //1.将密码进行md5加密
        String password = admin.getPassword();
        //DigestUtils 加密工具类,md5DigestAsHex将字节码进行md5加密
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        //2.查询数据库比对
        //查用户名
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, admin.getUsername());
        Admin ad = getOne(wrapper);

        //密码比对
        if (ad == null || !(ad.getPassword().equals(password))) {
            return Result.fail("登陆失败");
        }
        //4. 登陆成功, 随机生成token, 并将token和对应的admin信息保存在Redis中, 并把token返回给前端
        var adminTokenDto = BeanUtil.copyProperties(ad, AdminTokenDto.class,"hidePhone");
        var phone = ad.getPhone();
        adminTokenDto.setHidePhone(phone.substring(0, 2) + "* **** " + phone.substring(7));
        var token = UUID.randomUUID().toString(true);
        var key = LOGIN_ADMIN_TOKEN_KEY + token;
        var adminMap = BeanUtil.beanToMap(
                adminTokenDto,
                new HashMap<>(),
                CopyOptions
                        .create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((mapKey,mapField)->mapField.toString())
        );
        //将信息保存在Redis中
        stringRedisTemplate.opsForHash().putAll(key,adminMap);
        stringRedisTemplate.expire(key,30, TimeUnit.MINUTES);
        return Result.ok(token);
    }

    @Override
    public Result logout(HttpServletRequest request) {
        //获取token
        var token = request.getHeader(REQUEST_AUTHORIZATION_HEADER).split(" ")[1];
        //删除Redis中的信息
        var key = LOGIN_ADMIN_TOKEN_KEY + token;
        var success = stringRedisTemplate.delete(key);
        if(BooleanUtil.isTrue(success)){
            return Result.ok();
        }
        return Result.fail("退出失败");
    }

    @Override
    public Result getDtoByThreadLocal() {
        var adminTokenDto = AdminHolder.getAdmin();
        return Result.ok(adminTokenDto);
    }

    @Override
    public Result sendMsg(Admin admin) {
        var one = getById(admin.getId());
        var phone = one.getPhone();
        if (!phone.equals(admin.getPhone())) {
            throw new CustomException("手机号码与账户不符");
        }
        //生成随机四位验证码
        var validateCode = ValidateCodeUtils.generateValidateCode(4).toString();
        log.info("验证码: {}", validateCode);
        //调用阿里云短信服务SMS API发送短信
        try {
            // SMSUtils.sendMessage(phone, validateCode);
        } catch (Exception e) {
            throw new SystemException("SMS service error");
        }

        //将生成的验证码存到redis中并设置有效期为5分钟
        var key = VALIDATE_ADMIN_RESET + admin.getPhone();
        stringRedisTemplate.opsForValue().set(key,validateCode,5, TimeUnit.MINUTES);

        return Result.ok("手机验证码发送成功");
    }

    @Override
    public Result updateAdminInfo(AdminDto adminDto,HttpServletRequest request) {
        //1.Redis中根据手机号获取对应验证码
        var key = VALIDATE_ADMIN_RESET + adminDto.getPhone();
        var valiCode = stringRedisTemplate.opsForValue().get(key);
        if(null==valiCode){
            //没有查询到手机号对应验证码
            throw new CustomException("手机号输入有误");
        }
        if(!valiCode.equals(adminDto.getCode())){
            //验证码错误
            throw new CustomException("验证码错误");
        }
        //手机号和验证码均正确,设置md5加密并更新密码
        adminDto.setPassword(DigestUtils.md5DigestAsHex(adminDto.getPass().getBytes()));
        updateById(adminDto);
        //删除Redis用户信息来重新登陆
        logout(request);
        return Result.ok("密码更新成功");
    }
}




