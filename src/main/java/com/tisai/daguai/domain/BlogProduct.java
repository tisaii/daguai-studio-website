package com.tisai.daguai.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 
 * TableName blog_product
 */
@TableName(value ="blog_product")
@Data
public class BlogProduct implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 博客id
     */
    private Long blogId;

    /**
     * 产品id
     */
    private Long productId;

    /**
     * 产品名称(冗余字段)
     */
    private String name;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}