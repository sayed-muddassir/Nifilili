//package com.nifilili.core.config;
//
//import com.nifilili.core.constants.SwaggerConstants;
//import org.springdoc.core.models.GroupedOpenApi;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class SwaggerGroupingConfig {
//
//    @Bean
//    public GroupedOpenApi authApi() {
//        return GroupedOpenApi.builder()
//                .group(SwaggerConstants.AUTH_1)
//                .group(SwaggerConstants.AUTH_2)
//                .packagesToScan("com.nifilili.auth")
//                .build();
//    }
//
//    @Bean
//    public GroupedOpenApi businessApi() {
//        return GroupedOpenApi.builder()
//                .group(SwaggerConstants.BUSINESS_1)
//                .group(SwaggerConstants.BUSINESS_2)
//                .group(SwaggerConstants.BUSINESS_3)
//                .packagesToScan("com.nifilili.business")
//                .build();
//    }
//}
