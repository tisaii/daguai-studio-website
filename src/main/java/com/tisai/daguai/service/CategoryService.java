package com.tisai.daguai.service;

import com.tisai.daguai.domain.Category;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tisai.daguai.dto.Result;

/**
 *
 */
public interface CategoryService extends IService<Category> {

    Result pageInfoWithCache(int page, int pageSize);

    Category getCategoryById(Long id);

    Result add(Category category);

    Result updateWithCacheById(Category category);

    Result deleteById(Long id);

    Result listByType(int type);
}
