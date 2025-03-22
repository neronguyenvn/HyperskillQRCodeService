package qrcodeapi.model

enum class QrCorrectionLevel {
    L, M, Q, H;

    companion object {
        val default = L
    }
}
