package com.tisai.daguai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tisai.daguai.domain.Admin;
import com.tisai.daguai.dto.AdminDto;
import com.tisai.daguai.dto.Result;

import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public interface AdminService extends IService<Admin> {

    Result login(Admin admin);

    Result logout(HttpServletRequest request);

    Result getDtoByThreadLocal();

    Result sendMsg(Admin admin) throws Exception;

    Result updateAdminInfo(AdminDto adminDto,HttpServletRequest request);
}
