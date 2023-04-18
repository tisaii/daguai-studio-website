package com.tisai.daguai.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ZSETDto {
    private String id;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
