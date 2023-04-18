package com.tisai.daguai.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    private Boolean success;
    private String msg;
    private Object data;
    private Long total;
    private Integer code;

    public static Result ok(){
        return new Result(true, null, null, null,1);
    }
    public static Result ok(Object data){
        return new Result(true, null, data, null,1);
    }
    public static Result fail(String errorMsg){
        return new Result(false, errorMsg, null, null,0);
    }
}
