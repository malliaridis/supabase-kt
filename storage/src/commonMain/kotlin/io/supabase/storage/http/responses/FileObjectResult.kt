package io.supabase.storage.http.responses

import io.supabase.storage.types.FileObject
import kotlinx.serialization.Serializable

@Serializable
sealed class FileObjectResult {

    @Serializable
    data class Success(val data: List<FileObject>) : FileObjectResult()

    @Serializable
    data class Failure(val error: String) : FileObjectResult()
}
