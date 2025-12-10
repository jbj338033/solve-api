package kr.solve.global.security.config

import kr.solve.global.security.filter.JwtAuthenticationFilter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import reactor.core.publisher.Mono

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    @Value("\${cors.allowed-origins:*}")
    private val allowedOrigins: String,
) {
    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain =
        http
            .csrf { it.disable() }
            .cors { it.configurationSource(corsConfigurationSource()) }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .logout { it.disable() }
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
            .exceptionHandling {
                it.authenticationEntryPoint(unauthorizedEntryPoint())
                it.accessDeniedHandler(accessDeniedHandler())
            }.authorizeExchange {
                it.pathMatchers("/auth/**").permitAll()
                it.pathMatchers("/ws/**").permitAll()
                it.pathMatchers("/api-docs/**").permitAll()
                it.pathMatchers("/swagger-ui/**").permitAll()
                it.pathMatchers("/swagger-ui.html").permitAll()
                it.pathMatchers("/webjars/**").permitAll()
                it.pathMatchers("/actuator/health").permitAll()
                it.pathMatchers(HttpMethod.GET, "/problems/**").permitAll()
                it.pathMatchers(HttpMethod.GET, "/tags/**").permitAll()
                it.pathMatchers(HttpMethod.GET, "/contests/**").permitAll()
                it.pathMatchers(HttpMethod.GET, "/submissions/**").permitAll()
                it.pathMatchers(HttpMethod.GET, "/users/**").permitAll()
                it.pathMatchers(HttpMethod.GET, "/workbooks/**").permitAll()
                it.pathMatchers(HttpMethod.GET, "/banners/**").permitAll()
                it.anyExchange().authenticated()
            }.addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION)
            .build()

    private fun unauthorizedEntryPoint() =
        ServerAuthenticationEntryPoint { exchange, _ ->
            exchange.response.statusCode = HttpStatus.UNAUTHORIZED
            Mono.empty()
        }

    private fun accessDeniedHandler() =
        ServerAccessDeniedHandler { exchange, _ ->
            exchange.response.statusCode = HttpStatus.FORBIDDEN
            Mono.empty()
        }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration =
            CorsConfiguration().apply {
                allowedOrigins = this@SecurityConfig.allowedOrigins.split(",").map { it.trim() }
                allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                allowedHeaders = listOf("*")
                allowCredentials = true
                maxAge = 3600L
            }
        return UrlBasedCorsConfigurationSource().apply {
            registerCorsConfiguration("/**", configuration)
        }
    }
}
