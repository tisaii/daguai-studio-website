package com.tisai.daguai.dto;

import com.tisai.daguai.domain.Blog;
import com.tisai.daguai.domain.BlogProduct;
import com.tisai.daguai.domain.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class BlogDto extends Blog {
    /**
     * 博客包含产品列表
     */
    private List<BlogProduct> blogProducts;
    /**
     * 分类名称
     */
    private String CategoryName;
    /**
     * 产品名格式化后的名称
     */
    private String productsName;
}
