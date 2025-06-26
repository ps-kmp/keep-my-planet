package pt.isel.keepmyplanet.ui.report.states

data class SelectedImage(
    val data: ByteArray,
    val filename: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SelectedImage

        if (!data.contentEquals(other.data)) return false
        if (filename != other.filename) return false

        return true
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + filename.hashCode()
        return result
    }
}
