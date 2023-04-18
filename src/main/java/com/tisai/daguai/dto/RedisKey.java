package com.tisai.daguai.dto;

import lombok.Getter;

/**
 * Redis Key dto对象
 * @param <ID>
 */
@Getter
public class RedisKey<ID> {
    private final ID id;
    private final String keyPrefix;
    private final String keySuffix;

    private RedisKey(ID id,String keyPrefix,String keySuffix){
        this.id=id;
        this.keyPrefix=keyPrefix;
        this.keySuffix=keySuffix;
    }

    public static<ID> RedisKey<ID> create(ID id,String keyPrefix,String keySuffix){
        return new RedisKey<>(id, keyPrefix, keySuffix);
    }

    public String getKey(){
        return this.keyPrefix+this.id.toString()+this.keySuffix;
    }
}
