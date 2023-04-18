package com.tisai.daguai.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * 读取SMS配置文件的类
 * Component:与Controller等一样,表示把该类交给springboot容器管理
 * PropertySource:value表示配置文件资源所在位置,encoding表示编码方式,配置文件中有中文时要设为UTF-8不然会乱码
 * ConfigurationProperties:对配置文件进行配置,prefix代表忽略前缀,这样下面读取时就不用加上前面一串,而是直接写变量名,注意该项必须全小写
 */
@Component
@PropertySource(value = "classpath:SMS.yml",encoding = "UTF-8")
@ConfigurationProperties(prefix = "sms")
@Getter
public class SMSConfig {
    @Value("${signName}")
    private String signName;
    @Value("${templateCode}")
    private String templateCode;
    @Value("${accessKeyId}")
    private String accessKeyId;
    @Value("${accessKeySecret}")
    private String accessKeySecret;

    public static String SIGN_NAME;
    public static String TEMPLATE_CODE;
    public static String ACCESSKEY_ID;
    public static String ACCESSKEY_SECRET;

    @PostConstruct
    private void setSMS(){
        SIGN_NAME=this.signName;
        TEMPLATE_CODE=this.templateCode;
        ACCESSKEY_ID=this.accessKeyId;
        ACCESSKEY_SECRET=this.accessKeySecret;
    }
}
