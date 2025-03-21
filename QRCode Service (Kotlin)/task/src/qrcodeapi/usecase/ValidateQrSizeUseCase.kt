package qrcodeapi.usecase

import org.springframework.stereotype.Component

@Component
class ValidateQrSizeUseCase {

    operator fun invoke(size: Int): Result<Unit> {
        val result =
            if (size in MIN_SIZE..MAX_SIZE) Result.success(Unit)
            else Result.failure(IllegalArgumentException(ERROR_MESSAGE))

        return result
    }

    companion object {
        private const val MIN_SIZE = 150
        private const val MAX_SIZE = 350
        private const val ERROR_MESSAGE = "Image size must be between $MIN_SIZE and $MAX_SIZE pixels"
    }
}
