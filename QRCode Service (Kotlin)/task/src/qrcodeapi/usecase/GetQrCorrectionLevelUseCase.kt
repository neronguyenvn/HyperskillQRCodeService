package qrcodeapi.usecase

import org.springframework.stereotype.Component
import qrcodeapi.model.QrCorrectionLevel

@Component
class GetQrCorrectionLevelUseCase {

    operator fun invoke(correction: String): Result<QrCorrectionLevel> {
        return runCatching { Result.success(QrCorrectionLevel.valueOf(correction)) }
            .getOrElse { Result.failure(IllegalArgumentException(ERROR_MESSAGE)) }
    }

    companion object {
        private const val ERROR_MESSAGE = "Permitted error correction levels are L, M, Q, H"
    }
}
