package qrcodeapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.BufferedImageHttpMessageConverter
import org.springframework.http.converter.HttpMessageConverter
import java.awt.image.BufferedImage

@SpringBootApplication
class QRCodeApplication {

    @Bean
    fun bufferedImageHttpMessageConverter(): HttpMessageConverter<BufferedImage> {
        return BufferedImageHttpMessageConverter()
    }
}

fun main(args: Array<String>) {
    runApplication<QRCodeApplication>(*args)
}
