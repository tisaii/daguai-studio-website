package com.tisai.daguai.service;

import com.tisai.daguai.domain.Feedback;
import com.baomidou.mybatisplus.extension.service.IService;
import com.tisai.daguai.dto.Result;

import java.time.LocalDateTime;
import java.util.List;

/**
 *
 */
public interface FeedbackService extends IService<Feedback> {

    Result pageWithRedis(int page, int pageSize, LocalDateTime beginTime, LocalDateTime endTime, String contain, String status);

    Result reply(Feedback feedback);

    Result delete(List<Long> ids);

    Result add(Feedback feedback);
}
