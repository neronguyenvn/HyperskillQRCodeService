package qrcodeapi.repository

import org.springframework.stereotype.Component
import java.awt.Color
import java.awt.image.BufferedImage

@Component
class QrCodeRepository {

    fun generateQrCode(): BufferedImage {
        val image = BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB)
        image.createGraphics().apply {
            color = Color.WHITE
            fillRect(0, 0, WIDTH, HEIGHT)
        }

        return image
    }

    companion object {
        private const val WIDTH = 250
        private const val HEIGHT = 250
    }
}
