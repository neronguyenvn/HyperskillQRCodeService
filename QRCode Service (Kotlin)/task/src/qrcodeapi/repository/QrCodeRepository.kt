package qrcodeapi.repository

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import org.springframework.stereotype.Component
import java.awt.image.BufferedImage

@Component
class QrCodeRepository {

    fun generateQrCode(size: Int, contents: String): BufferedImage {
        val bitMatrix = QRCodeWriter().encode(contents, BarcodeFormat.QR_CODE, size, size)
        return MatrixToImageWriter.toBufferedImage(bitMatrix)
    }
}
