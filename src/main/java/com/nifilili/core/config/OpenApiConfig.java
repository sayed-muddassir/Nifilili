package com.nifilili.core.config;

import com.nifilili.core.constants.SwaggerConstants;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
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
                @Tag(name = SwaggerConstants.BUSINESS_0, description = "Admin management of province, district, and municipality masters."),
                @Tag(name = SwaggerConstants.BUSINESS_1, description = "Admin APIs to manage business onboarding master definitions in a predictable sequence."),
                @Tag(name = SwaggerConstants.BUSINESS_2, description = "Chronological onboarding APIs: fetch config, create business, complete profile, upload docs, submit for review."),
                @Tag(name = SwaggerConstants.BUSINESS_3, description = "Public business discovery APIs."),
                @Tag(name = SwaggerConstants.BUSINESS_5, description = "Admin and User APIs for storing and serving files from backend local storage."),
                @Tag(name = SwaggerConstants.KYC_1, description = "KYC management endpoints for admin users to review and manage KYC applications."),
                @Tag(name = SwaggerConstants.KYC_2, description = "KYC owner-facing endpoints: check verification status, view correction banner, and uploaded documents."),
                @Tag(name = SwaggerConstants.KYC_3, description = "KYC public endpoint for checking business verification badge status."),
//                @Tag(name = SwaggerConstants.OFFERING_1),
//                @Tag(name = SwaggerConstants.OFFERING_2),
//                @Tag(name = SwaggerConstants.OFFERING_3),
//                @Tag(name = SwaggerConstants.JOB_1),
//                @Tag(name = SwaggerConstants.JOB_2),
//                @Tag(name = SwaggerConstants.JOB_3),
//                @Tag(name = SwaggerConstants.JOB_4),
//                @Tag(name = SwaggerConstants.ORDER_1),
//                @Tag(name = SwaggerConstants.ORDER_2),
//                @Tag(name = SwaggerConstants.ORDER_3),
//                @Tag(name = SwaggerConstants.ORDER_4),
//                @Tag(name = SwaggerConstants.ORDER_5),
//                @Tag(name = SwaggerConstants.ORDER_6),
//                @Tag(name = SwaggerConstants.ORDER_7),
//                @Tag(name = SwaggerConstants.ORDER_8),
//                @Tag(name = SwaggerConstants.ORDER_9),
//                @Tag(name = SwaggerConstants.ORDER_10),
//                @Tag(name = SwaggerConstants.ORDER_11),
//                @Tag(name = SwaggerConstants.ORDER_12)
        },
        security = @SecurityRequirement(name = "Bearer Authentication")
)
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {}
