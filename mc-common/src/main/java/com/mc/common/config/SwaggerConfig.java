package com.mc.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger 配置
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("月结报表管理系统 API")
                        .description("月结报表管理与校验内部系统 API 文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MC Team")
                                .email("mc-team@example.com")));
    }
}
