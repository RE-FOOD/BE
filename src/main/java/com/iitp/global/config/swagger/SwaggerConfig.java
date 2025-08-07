package com.iitp.global.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityScheme.Type;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class SwaggerConfig {

    private static final String JWT_SCHEME_NAME = "JWT bearer-key";

    SecurityScheme bearerScheme = new SecurityScheme()
            .type(Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .description("JWT 토큰 키를 입력해주세요!");

    SecurityRequirement requirement = new SecurityRequirement().addList(JWT_SCHEME_NAME);

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .components(new Components().addSecuritySchemes(JWT_SCHEME_NAME, bearerScheme))
                .addSecurityItem(requirement)
                .info(new Info()
                        .title("RE:FOOD API 문서")
                        .description("\uD83C\uDF31 Walk, Eat, Save \uD83C\uDF0E 환경과 함께하는 똑똑한 한 끼 RE:FOOD API 명세서입니다.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("RE:FOOD")
                                .url("https://github.com/RE-FOOD"))
                )
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("개발 서버")
                ));
    }

}


