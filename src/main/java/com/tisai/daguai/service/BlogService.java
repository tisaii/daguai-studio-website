package com.tisai.daguai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.tisai.daguai.domain.Blog;
import com.tisai.daguai.dto.BlogDto;
import com.tisai.daguai.dto.Result;

import java.util.List;

/**
 *
 */
public interface BlogService extends IService<Blog> {


    /**
     * 根据博客名称或博客内容分页查询博客
     * @param page page
     * @param pageSize pagesize
     * @param name name
     * @return r
     */
    Result pageBynameWithRedis(int page, int pageSize, String name,String categoryId);

    /**
     * 根据博客id查询博客及其关联产品
     * @param id id
     * @return r
     */
    BlogDto getDtoById(Long id);


    /**
     * 根据博客id查询博客及其关联产品
     * @param id id
     * @return r
     */
    Result getDtoByIdWithRedis(Long id);

    /**
     * 根据id更新博客内容及其包含产品信息
     * @param blogDto blogDto
     * @return r
     */
    Result updateDtoWithRedis(BlogDto blogDto);

    /**
     * 根据博客id批量删除博客
     * @param ids ids
     * @return r
     */
    Result deleteDtoByIds(List<Long> ids);

    /**
     * 添加博客
     * @param blogDto blogDto
     */
    Result addBlogDtoWithRedis(BlogDto blogDto);
}
