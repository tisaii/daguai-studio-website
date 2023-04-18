package com.tisai.daguai.mapper;

import com.tisai.daguai.domain.Feedback;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tisai.daguai.dto.ZSETDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Entity com.tisai.daguai.domain.Feedback
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<Feedback> {
    //get id and createTime
    List<ZSETDto> selectIdAndCreateTimeZsetDtos();
    //get id and updateTime
    List<ZSETDto> selectIdAndUpdateTimeZsetDtos();
}




