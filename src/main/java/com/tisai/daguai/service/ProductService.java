package com.tisai.daguai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tisai.daguai.domain.Product;
import com.tisai.daguai.dto.Result;

import java.util.List;

/**
 *
 */
public interface ProductService extends IService<Product> {

    /**
     * 根据名称分页查询
     * @param page page
     * @param pageSize pageSize
     * @param name name
     * @return r
     */
    Result pageWithRedis(int page, int pageSize, String name, String type);

    Result saveWithRedis(Product product);

    Result getOneById(Long id);

    Result updateWithRedis(Product product);

    Result deleteByIdsWithRedis(List<Long> ids);

    Result listByCategoryId(Long categoryId);
}
