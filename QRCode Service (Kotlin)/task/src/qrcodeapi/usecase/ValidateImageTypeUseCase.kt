package qrcodeapi.usecase

import org.springframework.stereotype.Component

@Component
class ValidateImageTypeUseCase {

    operator fun invoke(type: String): Result<Unit> {
        val result =
            if (type in ALLOWED_TYPES) Result.success(Unit)
            else Result.failure(IllegalArgumentException(ERROR_MESSAGE))

        return result
    }

    companion object {
        const val DEFAULT_TYPE = "png"

        private val ALLOWED_TYPES = setOf("png", "jpeg", "gif")
        private const val ERROR_MESSAGE = "Only png, jpeg and gif image types are supported"
    }
}
