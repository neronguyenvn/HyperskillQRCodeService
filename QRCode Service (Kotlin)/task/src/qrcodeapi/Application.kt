package qrcodeapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class QRCodeApplication

fun main(args: Array<String>) {
    runApplication<QRCodeApplication>(*args)
}
