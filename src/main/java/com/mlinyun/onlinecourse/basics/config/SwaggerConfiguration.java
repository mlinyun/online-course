package com.mlinyun.onlinecourse.basics.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfiguration {

    // 扫描路径
    private static final String basePackage = "com.mlinyun.onlinecourse.data.controller";
    // 请求头名称
    private static final String headerName = "token";

    @Bean
    public GroupedOpenApi group01() {
        return GroupedOpenApi.builder()
                .group("数据中心接口")
                .addOperationCustomizer((operation, handlerMethod) -> {
                    operation.addSecurityItem(new SecurityRequirement().addList(headerName));
                    return operation;
                })
                .packagesToScan(basePackage)
                .build();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components();
        // 添加右上角的统一安全认证
        components.addSecuritySchemes(headerName,
                new SecurityScheme()
                        .type(SecurityScheme.Type.APIKEY)
                        .scheme("basic")
                        .name(headerName)
                        .in(SecurityScheme.In.HEADER)
                        .description("请求头")
        );
        return new OpenAPI()
                .components(components)
                .info(apiInfo())
                .externalDocs(externalDocumentation());
    }

    /**
     * 创建该 API 的基本信息（这些基本信息会展现在文档页面中）
     */
    private Info apiInfo() {
        Contact contact = new Contact();
        contact.setEmail("1938985998@qq.com");
        contact.setName("mlinyun");
        contact.setUrl("https://github.com/mlinyun");
        return new Info()
                .title("在线课程教学系统API文档")
                .description("本软件是基于 Vue 和 Spring Boot 开发的在线课程教学系统，是一种结合了前端框架 Vue.js 和 后端框架 SpringBoot 的在线教育解决方案")
                .version("v1.0.0")
                .contact(contact)
                .license(new License().name("Apache 2.0").url("https://springdoc.org"));
    }

    private ExternalDocumentation externalDocumentation() {
        return new ExternalDocumentation()
                .description("项目 GitHub 地址")
                .url("https://github.com/mlinyun/online-course");
    }
}
