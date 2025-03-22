package qrcodeapi.usecase

import org.springframework.stereotype.Component

@Component
class ValidateQrContentsUseCase {

    operator fun invoke(contents: String): Result<Unit> {
        val result =
            if (contents.isNotBlank()) Result.success(Unit)
            else Result.failure(IllegalArgumentException(ERROR_MESSAGE))

        return result
    }

    companion object {
        private const val ERROR_MESSAGE = "Contents cannot be null or blank"
    }
}
