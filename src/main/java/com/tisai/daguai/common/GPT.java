package com.tisai.daguai.common;

import lombok.Data;

import java.util.List;

@Data
public class GPT {
    /**
     * 模式
     */
    private String model;

    /**
     * message内容
     */
    private List<Message> messages;

    /**
     * 最大回复单词数
     */
    private Integer max_tokens;

    public GPT(String model, Integer max_tokens) {
        this.model = model;
        this.max_tokens = max_tokens;
    }
//    public void setMessage(String role,String content) {
//        this.message.setRole(role);
//        this.message.setContent(content);
//    }
}

