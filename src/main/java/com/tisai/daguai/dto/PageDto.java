package com.tisai.daguai.dto;

import lombok.Data;
import lombok.NonNull;

import java.util.List;

/**
 * 分页查询dto类
 */
@Data
public class PageDto<T> {
    //当前页码
    private final int page;
    //每页大小
    private final int pageSize;
    //数据总数
    private final Long total;
    //数据列表
    private List<T> dataList;

    public PageDto(int page, int pageSize, Long total) {
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }

    public PageDto(int page, int pageSize) {
        this.total=0L;
        this.page = page;
        this.pageSize = pageSize;
    }

    /**
     * 设置dataList并可链式调用原对象返回
     * @param dataList dataList
     * @return this
     */
    public PageDto<T> list(@NonNull List<T> dataList){
        this.dataList=dataList;
        return this;
    }

    /**
     * 获得一个空的PageDto对象
     * @param <T> elementType
     * @return empty PageDto
     */
    public static<T> PageDto<T> empty(){
        var pageDto = new PageDto<T>(0,0);
        pageDto.setDataList(List.of());
        return pageDto;
    }

    /**
     * 获取分页查询元素起始索引
     * @return 起始元素索引
     */
    public int getStart(){
        return (this.page-1)*this.pageSize;
    }

    /**
     * 获取分页查询元素结束索引
     * @return 结束元素索引
     */
    public int getEnd(){
        return this.page*this.pageSize-1;
    }

}
