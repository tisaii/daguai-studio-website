package com.tisai.daguai.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import com.tisai.daguai.common.JacksonObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.List;

@Slf4j
@Configuration
//启动swagger
@EnableSwagger2
//启动Knife4j
@EnableKnife4j
public class WebMvcConfig extends WebMvcConfigurationSupport {
    /**
     * 设置静态资源映射,即将浏览器地址映射到相应的页面资源文件
     * @Param registry
     */
    @Override
    protected void addResourceHandlers(ResourceHandlerRegistry registry) {
        log.info("开始进行资源映射");
        registry.addResourceHandler("/doc.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
        //将浏览器中地址为/backend/下的所有资源映射(定向)到相对路径/backend/下的资源文件
        registry.addResourceHandler("/admin/**").addResourceLocations("classpath:/admin/");
        registry.addResourceHandler("/user/**").addResourceLocations("classpath:/user/");

    }

    /**
     * 扩展mvc框架的消息转换器
     * @param converters converters
     */
    @Override
    protected void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
        log.info("扩展消息转换器");
        //创建消息转换器对象
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        //设置对象转换器,底层使用Jackson,将对象转化为json
        messageConverter.setObjectMapper(new JacksonObjectMapper());
        //将上面的消息转换器追加到mvc框架的转换器集合中
        //将自己的消息转换器放到序号为0来优先使用
        converters.add(0,messageConverter);
        super.extendMessageConverters(converters);
    }

    /**
     * 定义swagger文档类,设置swagger文档配置
     * @return docket
     */
    @Bean
    public Docket createRestApi() {
        // 文档类型
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo()) //接口信息,包括名称版本描述等
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.tisai.daguai.controller")) //设置接口 包的位置以扫描到接口
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("大怪工作室")
                .version("1.0")
                .description("大怪工作室产品文档")
                .build();
    }
}
