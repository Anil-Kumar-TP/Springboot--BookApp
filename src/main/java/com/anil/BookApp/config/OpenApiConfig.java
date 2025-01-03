package com.anil.BookApp.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(info = @Info(contact = @Contact(name = "Anil",email = "anil6996f@gmail.com",url = "http://github:Anil-Kumar-TP"),description = "OpenApi documentation for Spring Security",title = "OpenApi specification",version = "1.0",license = @License(name = "license name",url = "https://someurl.com"),termsOfService = "Terms of service"),servers = {@Server(description = "Local ENV",url = "http://localhost:8088/api/v1"),@Server(description = "Prod ENV",url = "http://anil.com")},security = @SecurityRequirement(name = "bearerAuth"))
@SecurityScheme(name = "bearerAuth",description = "JWT auth description",scheme = "bearer",type = SecuritySchemeType.HTTP,bearerFormat = "JWT",in = SecuritySchemeIn.HEADER)
public class OpenApiConfig {

}