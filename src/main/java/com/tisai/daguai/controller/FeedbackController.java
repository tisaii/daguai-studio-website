package com.tisai.daguai.controller;

import com.tisai.daguai.domain.Feedback;
import com.tisai.daguai.dto.Result;
import com.tisai.daguai.service.FeedbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

import static com.tisai.daguai.common.JacksonObjectMapper.DEFAULT_DATE_TIME_FORMAT;

@RestController
@RequestMapping("/feedback")
@Slf4j
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

    /**
     * 根据开始时间,结束时间,包含内容分页查询反馈信息
     * @param page page
     * @param pageSize pageSize
     * @param beginTime beginTime
     * @param endTime endTime
     * @param contain contain
     * @param status status
     * @return r
     */
    @GetMapping("/page")
    public Result page(int page, int pageSize,
                       @DateTimeFormat(pattern = DEFAULT_DATE_TIME_FORMAT) LocalDateTime beginTime,
                       @DateTimeFormat(pattern = DEFAULT_DATE_TIME_FORMAT) LocalDateTime endTime,
                       String contain, String status) {
        return feedbackService.pageWithRedis(page,pageSize,beginTime,endTime,contain,status);
    }

    /**
     * 更新回复
     * @param feedback feedback
     * @return r
     */
    @PutMapping("/reply")
    public Result reply(@RequestBody Feedback feedback){
        return feedbackService.reply(feedback);
    }

    /**
     * 根据ids进行批量删除
     * @param ids ids
     * @return r
     */
    @DeleteMapping
    public Result delete(@RequestParam("ids") List<Long> ids){
        return feedbackService.delete(ids);
    }

    /**
     * 用户端添加反馈
     * @param feedback feedback
     * @return r
     */
    @PostMapping
    public Result add(@RequestBody Feedback feedback){
        return feedbackService.add(feedback);
    }
}
