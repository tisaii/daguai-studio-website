package com.tisai.daguai.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.tisai.daguai.common.CustomException;
import com.tisai.daguai.common.SystemException;
import com.tisai.daguai.domain.Feedback;
import com.tisai.daguai.dto.RedisKey;
import com.tisai.daguai.dto.Result;
import com.tisai.daguai.service.FeedbackService;
import com.tisai.daguai.mapper.FeedbackMapper;
import com.tisai.daguai.utils.RedisCacheUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tisai.daguai.utils.RedisContents.*;

/**
 *
 */
@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback>
        implements FeedbackService {

    @Resource
    private RedisCacheUtils redisCacheUtils;

    @Resource
    private FeedbackMapper feedbackMapper;

    private static final String CACHE_FEEDBACKS_IDS_UT = CACHE_PAGE_ZSET_FEEDBACKS_PREFIX+CACHE_ALL_FEEDBACK_IDS+UT_SUFFIX;
    private static final String CACHE_FEEDBACKS_IDS_CT = CACHE_PAGE_ZSET_FEEDBACKS_PREFIX+CACHE_ALL_FEEDBACK_IDS+CT_SUFFIX;

    @Override
    public Result pageWithRedis(int page, int pageSize, LocalDateTime beginTime, LocalDateTime endTime, String contain, String status) {
        //1.get parameters from input, set the min and max timestamp
        var feedbackPage = new Page<Feedback>(page, pageSize, 0);
        //2.get the feedback id list from redis
        var feedbackIdList = (List<String>)new ArrayList<String>();
        if(beginTime==null&&endTime==null){
            //default page select, order by updateTime
            feedbackIdList = redisCacheUtils.avalancheAndPenetrationWithPageZSET(
                    CACHE_FEEDBACKS_IDS_UT,
                    feedbackMapper::selectIdAndUpdateTimeZsetDtos, //select updateTime
                    0,
                    System.currentTimeMillis(),
                    CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        }else {
            //select with condition, so order by createTime
            feedbackIdList = redisCacheUtils.avalancheAndPenetrationWithPageZSET(
                    CACHE_FEEDBACKS_IDS_CT,
                    feedbackMapper::selectIdAndCreateTimeZsetDtos, //select createTime
                    beginTime != null ? beginTime.toEpochSecond(ZoneOffset.UTC) : 0,
                    endTime != null ? endTime.toEpochSecond(ZoneOffset.UTC) : System.currentTimeMillis(),
                    CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        }
        //2.1 get id list
        if (feedbackIdList == null || feedbackIdList.isEmpty()) {
            //blank cache, return empty page
            return Result.ok(feedbackPage);
        }
        //3.get the all eligible objects List of Feedback and set, filter the "contain" condition
        var feedbackList = feedbackIdList.stream()
                .map(this::getFeedbackWithRedis)
                .filter(i->judgeContainATypeCond(i,contain,status)) //filter the condition
                .collect(Collectors.toList());
        if (feedbackList.isEmpty()) {
            //no eligible data
            return Result.ok(feedbackPage);
        }
        //3.1 get and set total eligible count
        var total = feedbackList.size();
        feedbackPage.setTotal(total);
        //4.sub by page and return
        return Result.ok(feedbackPage.setRecords(feedbackList.subList((page-1)*pageSize,Math.min(page*pageSize,total))));
    }

    @Override
    @Transactional
    public Result reply(Feedback feedback) {
        if(feedback==null){
            throw new CustomException("非法输入");
        }
        //get now stamp
        var now = LocalDateTime.now();
        //set the stamp to feedback object
        feedback.setUpdateTime(now);
        //update database
        updateById(feedback);
        //set the timestamp to redis cache zset(if exist)
        var id = feedback.getId().toString();
        var success = redisCacheUtils.updateZSETMoreElement(CACHE_FEEDBACKS_IDS_UT, CACHE_FEEDBACK_PREFIX+id, id,now.toEpochSecond(ZoneOffset.UTC), CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        if(!success){
            throw new SystemException("Redis ZSET Update is error");
        }
        return Result.ok("更新成功");
    }

    @Override
    public Result delete(List<Long> ids) {
        if(ids==null||ids.isEmpty()){
            throw new CustomException("非法输入");
        }
        //remove the database datas
        var dbop = removeByIds(ids);
        if(!dbop){
            throw new SystemException("database table feedback delete opreation Error");
        }
        //remove the data in redis Zset and the contact cache
        try {
            ids.forEach(i->         //delete all datas contact each ids
            {
                if (!redisCacheUtils.deleteZSETMoreElement(CACHE_FEEDBACKS_IDS_UT,CACHE_FEEDBACK_PREFIX+i,i,CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES)) {
                    throw new SystemException("redis delete operation error");
                }
            });
        }catch (Exception e){
            //catch the excetion may be happend
            throw new SystemException(e.getMessage());
        }
        //no err and return ok
        return Result.ok("删除成功");
    }

    @Override
    public Result add(Feedback feedback) {
        if(feedback==null){
            throw new CustomException("非法输入");
        }
        //1.add to database
        // get now time
        var now = LocalDateTime.now();
        // set to entity and save it in database
        feedback.setCreateTime(now);
        feedback.setUpdateTime(now);
        save(feedback);
        //2.add to redis zset if exists
        //insert into ct
        var id = feedback.getId();
        var timestamp = now.toEpochSecond(ZoneOffset.UTC);
        var ct = redisCacheUtils.insertZSETMoreElement(CACHE_FEEDBACKS_IDS_CT, id, timestamp, CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        //insert into ut
        var ut = redisCacheUtils.insertZSETMoreElement(CACHE_FEEDBACKS_IDS_UT, id, timestamp, CACHE_PAGE_DEFAULT_TTL, TimeUnit.MINUTES);
        if(!ct||!ut){
            //err
            throw new SystemException("redis insert into zset operation err");
        }
        //3.return
        return Result.ok("添加成功");
    }

    /**
     * judge the target object of Feedback whether eligible
     * @param target target object
     * @param contain contain
     * @param status status
     * @return ture or false
     */
    private boolean judgeContainATypeCond(Feedback target, String contain, String status) {
        var question = Objects.requireNonNullElse(target.getQuestion(),"");
        var reply = Objects.requireNonNullElse(target.getReply(),"");
        return (status==null||status.equals(target.getStatus().toString()))
                &&
                (contain==null||question.contains(contain)||reply.contains(contain));
    }


    private <ID> Feedback getFeedbackWithRedis(ID id) {
        if (id == null) {
            return null;
        }
        var feedback = redisCacheUtils.avalancheAndPenetration(
                RedisKey.create(Long.valueOf(id.toString()), CACHE_FEEDBACK_PREFIX, CACHE_FEEDBACK_INFO_SUFFIX),
                Feedback.class,
                this::getById,
                CACHE_FEEDBACK_TTL,
                TimeUnit.MINUTES
        );
        if (feedback == null) {
            throw new SystemException("Redis查询单个元素Feedback异常");
        }
        return feedback;
    }
}




