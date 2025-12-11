package kr.solve.global.h2.config

import org.h2.tools.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
@Profile("local")
class H2ConsoleConfig {
    @Bean(initMethod = "start", destroyMethod = "stop")
    fun h2WebServer(): Server = Server.createWebServer("-web", "-webAllowOthers", "-webPort", "9092")
}
