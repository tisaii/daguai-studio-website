package com.tisai.daguai.controller;

import com.tisai.daguai.domain.Category;
import com.tisai.daguai.dto.Result;
import com.tisai.daguai.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Resource
    private CategoryService categoryService;

    /**
     * 分页查询分类
     * @param page page
     * @param pageSize pageSize
     * @return page<Category>
     */
    @GetMapping("page")
    public Result page(int page, int pageSize){
        return categoryService.pageInfoWithCache(page,pageSize);
    }

    /**
     * 增加分类
     * @param category 添加的category对象
     * @return msg
     */
    @PostMapping
    public Result add(@RequestBody Category category){
        return categoryService.add(category);
    }

    /**
     * 根据id更新分类
     * @param category 更新的category对象
     * @return msg
     */
    @PutMapping
    public Result updateById(@RequestBody Category category){
        return categoryService.updateWithCacheById(category);
    }

    /**
     * 根据id删除分类
     * @param id id
     * @return msg
     */
    @DeleteMapping
    public Result deleteById(Long id){
        return categoryService.deleteById(id);
    }

    /**
     * 根据分类类型查询分类列表
     * @param type 类型
     * @return List<Category>
     */
    @GetMapping("/list")
    public Result list(int type){
        return categoryService.listByType(type);
    }
}
