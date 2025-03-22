package qrcodeapi.controller

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import qrcodeapi.model.ErrorResponse
import qrcodeapi.model.QrCorrectionLevel
import qrcodeapi.repository.QrCodeRepository
import qrcodeapi.usecase.GetQrCorrectionLevelUseCase
import qrcodeapi.usecase.ValidateImageTypeUseCase
import qrcodeapi.usecase.ValidateQrContentsUseCase
import qrcodeapi.usecase.ValidateQrSizeUseCase

@RestController
class QRCodeRestController(
    private val qrCodeRepository: QrCodeRepository,
    private val validateQrContentsUseCase: ValidateQrContentsUseCase,
    private val validateQrSizeUseCase: ValidateQrSizeUseCase,
    private val getQrCorrectionLevelUseCase: GetQrCorrectionLevelUseCase,
    private val validateImageTypeUseCase: ValidateImageTypeUseCase,
) {

    @GetMapping("/api/health")
    fun ping() = Unit

    @GetMapping("/api/qrcode")
    fun getQrCode(
        @RequestParam contents: String,
        @RequestParam size: Int = ValidateQrSizeUseCase.DEFAULT_SIZE,
        @RequestParam correction: String = QrCorrectionLevel.default.name,
        @RequestParam type: String = ValidateImageTypeUseCase.DEFAULT_TYPE,
    ): ResponseEntity<Any> {

        validateQrContentsUseCase(contents).onFailure {
            return ResponseEntity
                .badRequest()
                .body(ErrorResponse(it.message.orEmpty()))
        }

        validateQrSizeUseCase(size).onFailure {
            return ResponseEntity
                .badRequest()
                .body(ErrorResponse(it.message.orEmpty()))
        }

        val parseCorrection = getQrCorrectionLevelUseCase(correction).getOrElse { error ->
            return ResponseEntity
                .badRequest()
                .body(ErrorResponse(error.message.orEmpty()))
        }

        validateImageTypeUseCase(type).onFailure {
            return ResponseEntity
                .badRequest()
                .body(ErrorResponse(it.message.orEmpty()))
        }

        val qrCode = qrCodeRepository.generateQrCode(size, contents, parseCorrection)
        return ResponseEntity
            .ok()
            .contentType(MediaType.parseMediaType("image/$type"))
            .body(qrCode)
    }
}
