package qrcodeapi.repository

import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import org.springframework.stereotype.Component
import qrcodeapi.model.QrCorrectionLevel
import java.awt.image.BufferedImage

@Component
class QrCodeRepository {

    fun generateQrCode(
        size: Int,
        contents: String,
        correction: QrCorrectionLevel
    ): BufferedImage {

        val bitMatrix = QRCodeWriter().encode(
            contents, BarcodeFormat.QR_CODE, size, size,
            mapOf(EncodeHintType.ERROR_CORRECTION to correction)
        )
        return MatrixToImageWriter.toBufferedImage(bitMatrix)
    }
}
