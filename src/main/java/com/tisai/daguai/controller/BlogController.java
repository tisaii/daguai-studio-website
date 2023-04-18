package com.tisai.daguai.controller;

import com.tisai.daguai.dto.BlogDto;
import com.tisai.daguai.dto.Result;
import com.tisai.daguai.service.BlogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/blog")
@Slf4j
public class BlogController {

    @Resource
    private BlogService blogService;

    /**
     * 根据姓名或内容分页查询博客
     * @param page page
     * @param pageSize pageSize
     * @param name name
     * @return r
     */
    @GetMapping("/page")
    public Result page(int page,int pageSize,String name,String type){
        return blogService.pageBynameWithRedis(page,pageSize,name,type);
    }

    /**
     * 添加博客
     * @param blogDto blogDto
     * @return r
     */
    @PostMapping
    public Result addBlog(@RequestBody BlogDto blogDto){
        return blogService.addBlogDtoWithRedis(blogDto);
    }

    /**
     * 根据id查询博客及其关联产品
     * @param id id
     * @return r
     */
    @GetMapping("/{id}")
    public Result getDtoById(@PathVariable Long id){
        return blogService.getDtoByIdWithRedis(id);
    }

    /**
     * 根据id更新博客内容及其包含产品信息
     * @param blogDto blogDto
     * @return r
     */
    @PutMapping
    public Result UpdateDtoWithRedis(@RequestBody BlogDto blogDto){
        return blogService.updateDtoWithRedis(blogDto);
    }

    /**
     * 根据博客id批量删除博客
     * @param ids ids
     * @return r
     */
    @DeleteMapping
    public Result deleteDto(@RequestParam("ids") List<Long> ids){
        return blogService.deleteDtoByIds(ids);
    }
}
