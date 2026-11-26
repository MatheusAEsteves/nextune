package com.nextune.common.config;

import com.nextune.common.storage.ArmazenamentoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS para o front (Angular) e publicação da pasta de uploads em /uploads/**.
 * Atenção: a configuração de segurança precisa ter http.cors(...) habilitado e liberar GET /uploads/**.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ArmazenamentoService armazenamento;
    private final String[] origensPermitidas;

    public WebConfig(ArmazenamentoService armazenamento,
                     @Value("${nextune.cors.allowed-origins:http://localhost:4200}") String[] origensPermitidas) {
        this.armazenamento = armazenamento;
        this.origensPermitidas = origensPermitidas;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(origensPermitidas)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String local = armazenamento.getRaiz().toUri().toString();
        if (!local.endsWith("/")) {
            local += "/";
        }
        // O handler de recursos estáticos já suporta HTTP Range (necessário para o <audio> avançar/voltar)
        registry.addResourceHandler("/uploads/**").addResourceLocations(local);
    }
}
