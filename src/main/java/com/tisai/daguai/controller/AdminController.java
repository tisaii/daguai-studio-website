package com.tisai.daguai.controller;

import com.tisai.daguai.common.R;
import com.tisai.daguai.domain.Admin;
import com.tisai.daguai.dto.AdminDto;
import com.tisai.daguai.dto.Result;
import com.tisai.daguai.service.AdminService;
import com.tisai.daguai.utils.ChatGptUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/account")
public class AdminController {
    @Resource
    private AdminService adminService;

    /**
     * 管理员登录
     *
     * @param admin admin
     * @return msg
     */
    @PostMapping("/login")
    public Result login(@RequestBody Admin admin) {
        return adminService.login(admin);
    }

    /**
     * 退出登录
     *
     * @param request request
     * @return msg
     */
    @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        return adminService.logout(request);
    }

    /**
     * 根据username查询用户信息
     *
     * @return msg
     */
    @GetMapping
    public Result getDtoByThreadLocal() {
        return adminService.getDtoByThreadLocal();
    }

    /**
     * 发送验证码
     *
     * @param admin admin
     * @return msg
     */
    @PostMapping("/sendMsg")
    public Result sendMsg(@RequestBody Admin admin) throws Exception {
        return adminService.sendMsg(admin);
    }

    /**
     * 更新账号信息
     * @param adminDto adminDto
     * @return msg
     */
    @PutMapping
    public Result update(@RequestBody AdminDto adminDto, HttpServletRequest request){
        return adminService.updateAdminInfo(adminDto,request);
    }

    /**
     * ChatGpt查询
     * @param question question
     * @return msg
     */
    @GetMapping("/search")
    public R<String> searchByGpt(String question) throws Exception {
        log.info(question);
        String reply = ChatGptUtils.reply(question);
        return R.success(reply);
    }
}
