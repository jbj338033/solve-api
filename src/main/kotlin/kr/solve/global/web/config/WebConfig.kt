package kr.solve.global.web.config

import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.converter.ConverterFactory
import org.springframework.format.FormatterRegistry
import org.springframework.web.reactive.config.WebFluxConfigurer

@Configuration
class WebConfig : WebFluxConfigurer {
    override fun addFormatters(registry: FormatterRegistry) {
        registry.addConverterFactory(StringToEnumConverterFactory())
    }
}

class StringToEnumConverterFactory : ConverterFactory<String, Enum<*>> {
    override fun <T : Enum<*>> getConverter(targetType: Class<T>): Converter<String, T> = StringToEnumConverter(targetType)

    private class StringToEnumConverter<T : Enum<*>>(
        private val enumType: Class<T>,
    ) : Converter<String, T> {
        override fun convert(source: String): T =
            enumType.enumConstants.firstOrNull { it.name.equals(source, ignoreCase = true) }
                ?: throw IllegalArgumentException("No enum constant ${enumType.simpleName}.$source")
    }
}
