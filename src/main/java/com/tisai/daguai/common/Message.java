package com.tisai.daguai.common;

import lombok.Data;

@Data
/*
  gpt消息类
 */
public class Message{
    private String role;

    private String content;

    public Message(String role, String content) {
        this.role = role;
        this.content = content;
    }
}
