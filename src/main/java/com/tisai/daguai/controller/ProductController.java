package com.tisai.daguai.controller;

import com.tisai.daguai.domain.Product;
import com.tisai.daguai.dto.Result;
import com.tisai.daguai.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {
    @Resource
    private ProductService productService;

    /**
     * 分页查询
     * @param page page
     * @param pageSize pageSize
     * @param name name
     * @param type categoryId
     * @return Page
     */
    @GetMapping("/page")
    public Result page(int page, int pageSize, String name, String type){
        return productService.pageWithRedis(page,pageSize,name,type);
    }

    /**
     * 添加产品或修改产品信息
     * @param product product
     * @return r
     */
    @PostMapping
    public Result save(@RequestBody Product product){
        return productService.saveWithRedis(product);
    }

    /**
     * 根据id查询单个产品数据
     * 用于数据回显
     * @param id id
     * @return productDto
     */
    @GetMapping("/{id}")
    public Result getOneById(@PathVariable Long id){
        return productService.getOneById(id);
    }

    /**
     * 根据id更新产品信息
     * @param product product
     * @return r
     */
    @PutMapping
    public Result update(@RequestBody Product product){
        return productService.updateWithRedis(product);
    }

    /**
     * 根据ids批量删除产品信息
     * @param ids ids
     * @return r
     */
    @DeleteMapping
    public Result delete(@RequestParam("ids") List<Long> ids){
        return productService.deleteByIdsWithRedis(ids);
    }

    /**
     * 根据分类id查询产品列表
     * @param categoryId categoryId
     * @return r
     */
    @GetMapping("/list")
    public Result listByCategoryId(Long categoryId){
        return productService.listByCategoryId(categoryId);
    }
}
