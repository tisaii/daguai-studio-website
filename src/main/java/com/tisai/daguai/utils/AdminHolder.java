package com.tisai.daguai.utils;

import com.tisai.daguai.dto.AdminTokenDto;

/**
 * 获取当前线程admin用户
 */
public class AdminHolder {
    private static final ThreadLocal<AdminTokenDto> ad = new ThreadLocal<>();

    public static void saveAdmin(AdminTokenDto admin) {ad.set(admin);}

    public static AdminTokenDto getAdmin(){
        return ad.get();
    }

    public static void removeAdmin(){ad.remove();}
}
