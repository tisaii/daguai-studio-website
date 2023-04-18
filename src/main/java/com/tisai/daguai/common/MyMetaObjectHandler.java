package com.tisai.daguai.common;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 自定义元数据处理器
 */
//@Component将此类交给Spring管理
@Component
@Slf4j
public class MyMetaObjectHandler implements MetaObjectHandler {
    /**
     * 插入操作自动填充
     * @param metaObject metaObject
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        metaObject.setValue("createTime", LocalDateTime.now());
        metaObject.setValue("updateTime", LocalDateTime.now());
    }

    /**
     * 更新操作自动填充
     * @param metaObject metaObject
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        //log.info("公共字段自动填充[UPDATE]...");
        //log.info(metaObject.toString());
        metaObject.setValue("updateTime", LocalDateTime.now());

    }
}
