package com.tisai.daguai.domain;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 * TableName product
 */
@TableName(value ="product")
@Data
public class Product implements Serializable {
    /**
     * 主键
     */
    @TableId
    private Long id;

    /**
     * 产品分类id
     */
    private Long categoryId;

    /**
     * 产品名称
     */
    private String name;

    /**
     * 图片名称
     */
    private String image;

    /**
     * 产品描述
     */
    private String description;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除,0未删除,1已删除
     */
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}