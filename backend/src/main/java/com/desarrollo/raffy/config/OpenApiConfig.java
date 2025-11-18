package com.desarrollo.raffy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI raffyOpenAPI() {
        Info info = new Info()
                .title("Raffy API")
                .description("Documentación Backend API de Raffy. Endpoints descritos con propósitos, parámetros y respuestas.")
                .version("v1")
                .contact(new Contact().name("Equipo Raffy").email("soporte@raffy.app"))
                .license(new License().name("Apache-2.0").url("https://www.apache.org/licenses/LICENSE-2.0"));

        Server local = new Server().url("/").description("Servidor principal");

        return new OpenAPI()
                .info(info)
                .servers(List.of(local))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .tags(List.of(
                        new Tag().name("Autenticación").description("Registro, inicio de sesión, perfil y gestión de usuarios registrados"),
                        new Tag().name("Eventos").description("Gestión de eventos: sorteos, rifas, concursos, participantes y ganadores"),
                        new Tag().name("Pagos").description("Creación, consulta y estado de pagos asociados a eventos y usuarios"),
                        new Tag().name("URLs").description("Acortador, enlaces de invitación, uso único y redirecciones")
                ));
    }
}