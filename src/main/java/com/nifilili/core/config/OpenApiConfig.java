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
                @Tag(name = SwaggerConstants.BUSINESS_3),
                @Tag(name = SwaggerConstants.BUSINESS_4),
                @Tag(name = SwaggerConstants.OFFERING_1),
                @Tag(name = SwaggerConstants.OFFERING_2),
                @Tag(name = SwaggerConstants.OFFERING_3),
                @Tag(name = SwaggerConstants.JOB_1),
                @Tag(name = SwaggerConstants.JOB_2),
                @Tag(name = SwaggerConstants.JOB_3),
                @Tag(name = SwaggerConstants.JOB_4),
                @Tag(name = SwaggerConstants.ORDER_1),
                @Tag(name = SwaggerConstants.ORDER_2),
                @Tag(name = SwaggerConstants.ORDER_3),
                @Tag(name = SwaggerConstants.ORDER_4),
                @Tag(name = SwaggerConstants.ORDER_5),
                @Tag(name = SwaggerConstants.ORDER_6),
                @Tag(name = SwaggerConstants.ORDER_7),
                @Tag(name = SwaggerConstants.ORDER_8),
                @Tag(name = SwaggerConstants.ORDER_9),
                @Tag(name = SwaggerConstants.ORDER_10),
                @Tag(name = SwaggerConstants.ORDER_11),
                @Tag(name = SwaggerConstants.ORDER_12)
        }
)
public class OpenApiConfig {}
