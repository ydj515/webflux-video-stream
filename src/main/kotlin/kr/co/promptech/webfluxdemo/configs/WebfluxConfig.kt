package kr.co.promptech.webfluxdemo.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.config.CorsRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class WebConfig : WebFluxConfigurer {
    @Bean
    fun webFluxConfigurer(): WebFluxConfigurer =
        object : WebFluxConfigurer {
            override fun addCorsMappings(registry: CorsRegistry) {
                registry
                    .addMapping("/**")
                    .allowedOrigins("*")
                    .allowedMethods("GET")
                    .allowedHeaders("*")
            }
        }
}
