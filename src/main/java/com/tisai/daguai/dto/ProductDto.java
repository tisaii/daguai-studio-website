package com.tisai.daguai.dto;

import com.tisai.daguai.domain.Product;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ProductDto extends Product {
    /**
     * 产品对应分类名称
     */
    private String CategoryName;

}
