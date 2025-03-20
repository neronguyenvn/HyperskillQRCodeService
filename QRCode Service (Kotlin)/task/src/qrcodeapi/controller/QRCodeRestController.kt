package qrcodeapi.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import qrcodeapi.repository.QrCodeRepository
import java.awt.image.BufferedImage

@RestController
class QRCodeRestController {

    @GetMapping("/api/health")
    fun ping() = Unit

    @GetMapping("/api/qrcode")
    fun getQrCode(qrCodeRepository: QrCodeRepository): ResponseEntity<BufferedImage> {
        val qrCode = qrCodeRepository.generateQrCode()
        return ResponseEntity
            .ok()
            .contentType(MediaType.IMAGE_PNG)
            .body(qrCode)
    }
}
