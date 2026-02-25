package com.nifilili.core.config;

import com.nifilili.core.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Marketplace API",
                version = "v1",
                description = "Complete Marketplace Platform APIs"
        ),
        tags = {
                @Tag(name = SwaggerConstants.AUTH_1),
                @Tag(name = SwaggerConstants.AUTH_2),
                @Tag(name = SwaggerConstants.BUSINESS_1),
                @Tag(name = SwaggerConstants.BUSINESS_2),
                @Tag(name = SwaggerConstants.BUSINESS_3)
        }
)
public class OpenApiConfig {}