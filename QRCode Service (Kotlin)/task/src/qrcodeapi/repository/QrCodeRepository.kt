package qrcodeapi.repository

import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.image.BufferedImage

@Component
class QrCodeRepository {

    fun generateQrCode(size: Int): BufferedImage {
        val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)
        image.createGraphics().apply {
            color = Color.WHITE
            fillRect(0, 0, size, size)
        }

        return image
    }
}
