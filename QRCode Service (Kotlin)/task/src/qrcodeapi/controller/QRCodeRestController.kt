package qrcodeapi.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class QRCodeRestController {

    @GetMapping("/api/health")
    fun ping() = Unit

    @GetMapping("/api/qrcode")
    fun getQrCode() = ResponseEntity
        .status(HttpStatus.NOT_IMPLEMENTED)
        .build<HttpStatus>()
}
