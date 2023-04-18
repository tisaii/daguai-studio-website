package com.tisai.daguai.dto;

import com.tisai.daguai.domain.Admin;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AdminDto extends Admin {
    /**
     * 隐藏的手机号
     */
    private String hidePhone;

    /**
     * 新密码
     */
    private String pass;

    /**
     * 验证码
     */
    private String code;
}
