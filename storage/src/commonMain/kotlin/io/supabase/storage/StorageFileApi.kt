package io.supabase.storage

import io.ktor.client.request.forms.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.core.*
import io.supabase.storage.http.FetchOptions
import io.supabase.storage.http.StorageHttpClient
import io.supabase.storage.json.deserialize
import io.supabase.storage.types.FileObject
import io.supabase.storage.types.FileOptions
import io.supabase.storage.types.SearchOptions
import io.supabase.storage.types.SortBy

val DEFAULT_SEARCH_OPTIONS = SearchOptions(
    limit = 100,
    offset = 0,
    sortBy = SortBy(
        column = "name",
        order = "asc"
    )
)

val DEFAULT_FILE_OPTIONS = FileOptions(
    cacheControl = "3600",
    contentType = "text/plain;charset=UTF-8",
    upsert = false
)

data class SignedUrlResponse(
    val data: String,
    val signedUrl: String
)

data class PublicUrlResponse(
    val data: String,
    val publicUrl: String
)

class StorageFileApi(
    private val bucketId: String,
    private val storageHttpClient: StorageHttpClient
) {

    /**
     * Uploads a file to an existing bucket or replaces an existing file at the specified path with a new one.
     *
     * @param method HTTP method.
     * @param path The relative file path. Should be of the format `folder/subfolder/filename.png`. The bucket must already exist before attempting to upload.
     * @param fileBody The body of the file to be stored in the bucket. Any of ArrayBuffer, ArrayBufferView, Blob, Buffer, File, FormData, NodeJS.ReadableStream, ReadableStream<Uint8Array>, URLSearchParams, string
     * @param fileOptions HTTP headers.
     * `cacheControl`: string, the `Cache-Control: max-age=<seconds>` seconds value.
     * `contentType`: string, the `Content-Type` header value. Should be specified if using a `fileBody` that is neither `Blob` nor `File` nor `FormData`, otherwise will default to `text/plain;charset=UTF-8`.
     * `upsert`: boolean, whether to perform an upsert.
     */
    private suspend fun uploadOrUpdate(
        method: HttpMethod,
        path: String,
        fileBody: PartData,
        fileOptions: FileOptions?
    ): String {

        if (method != HttpMethod.Post && method != HttpMethod.Put) {
            throw Error("Invalid method. Must bee Post or Put.")
        }


        val options = FileOptions(
            cacheControl = fileOptions?.cacheControl ?: DEFAULT_FILE_OPTIONS.cacheControl,
            contentType = fileOptions?.contentType ?: DEFAULT_FILE_OPTIONS.contentType,
            upsert = fileOptions?.upsert ?: DEFAULT_FILE_OPTIONS.upsert
        )

        val headers: Headers = headersOf()

        // TODO Add header "x-upsert": options.upsert if method is HttpMethod.Post
        // {
        //   ...this.headers,
        //   ...(method === 'POST' && { 'x-upsert': String(options.upsert as boolean) }),
        // }

        val body: Any = when (fileBody) {
            is PartData.BinaryItem -> {
                // TODO Handle properly blob
                FormDataContent(
                    formData = Parameters.build {
                        append("cacheControl", options.cacheControl!!)
                        append("", fileBody.toString())
                    }
                )
            }
            is PartData.FormItem -> {
                FormDataContent(formData = Parameters.build {
                    append(fileBody.name!!, fileBody.value)
                    append("cacheControl", options.cacheControl!!)
                })
            }
            is PartData.FileItem -> {
                // TODO Add headers for cache-control and content-type
                // headers['cache-control'] = `max-age=${options.cacheControl}`
                // headers['content-type'] = options.contentType as string
            }
            else -> {
                fileBody
                // TODO Add headers for cache-control and content-type
                // headers['cache-control'] = `max-age=${options.cacheControl}`
                // headers['content-type'] = options.contentType as string
            }
        }

        val finalPath = getFinalPath(path)

        if (method == HttpMethod.Post) {
            storageHttpClient.post(
                path = "/object/${finalPath}",
                body = body,
                options = FetchOptions(headers, true)
            )
        } else {
            storageHttpClient.put(
                path = "/object/${finalPath}",
                body = body,
                options = FetchOptions(headers, true)
            )
        }

        return finalPath
    }

    /**
     * Uploads a file to an existing bucket.
     *
     * @param path The relative file path. Should be of the format `folder/subfolder/filename.png`. The bucket must already exist before attempting to upload.
     * @param fileBody The body of the file to be stored in the bucket.
     * @param fileOptions HTTP headers.
     * `cacheControl`: string, the `Cache-Control: max-age=<seconds>` seconds value.
     * `contentType`: string, the `Content-Type` header value. Should be specified if using a `fileBody` that is neither `Blob` nor `File` nor `FormData`, otherwise will default to `text/plain;charset=UTF-8`.
     * `upsert`: boolean, whether to perform an upsert.
     */
    suspend fun upload(
        path: String,
        fileBody: PartData,
        fileOptions: FileOptions?
    ): String {
        return this.uploadOrUpdate(HttpMethod.Post, path, fileBody, fileOptions)
    }

    /**
     * Replaces an existing file at the specified path with a new one.
     *
     * @param path The relative file path. Should be of the format `folder/subfolder/filename.png`. The bucket must already exist before attempting to upload.
     * @param fileBody The body of the file to be stored in the bucket.
     * @param fileOptions HTTP headers.
     * `cacheControl`: string, the `Cache-Control: max-age=<seconds>` seconds value.
     * `contentType`: string, the `Content-Type` header value. Should be specified if using a `fileBody` that is neither `Blob` nor `File` nor `FormData`, otherwise will default to `text/plain;charset=UTF-8`.
     * `upsert`: boolean, whether to perform an upsert.
     */
    suspend fun update(
        path: String,
        fileBody: PartData,
        fileOptions: FileOptions?
    ): String {
        return this.uploadOrUpdate(HttpMethod.Put, path, fileBody, fileOptions)
    }

    /**
     * Moves an existing file, optionally renaming it at the same time.
     *
     * @param fromPath The original file path, including the current file name. For example `folder/image.png`.
     * @param toPath The new file path, including the new file name. For example `folder/image-copy.png`.
     */
    suspend fun move(
        fromPath: String,
        toPath: String
    ): String {
        return storageHttpClient.post(
            path = "/object/move",
            body = formData {
                append("bucketId", bucketId)
                append("sourceKey", fromPath)
                append("destinationKey", toPath)
            },
            options = FetchOptions(headersOf(), true)
        )
    }

    /**
     * Create signed url to download file without requiring permissions. This URL can be valid for a set number of seconds.
     *
     * @param path The file path to be downloaded, including the current file name. For example `folder/image.png`.
     * @param expiresIn The number of seconds until the signed URL expires. For example, `60` for a URL which is valid for one minute.
     */
    suspend fun createSignedUrl(
        path: String,
        expiresIn: Long
    ): String {
        val finalPath = this.getFinalPath(path)
        val data = storageHttpClient.post(
            path = "/object/sign/${finalPath}",
            options = FetchOptions(headersOf(), true),
            body = null
            // TODO See where to pass expiresIn
//            ??? = { expiresIn }
        )
        return "${this.storageHttpClient.url}${data}" // Signed URL
    }

    /**
     * Downloads a file.
     *
     * @param path The file path to be downloaded, including the path and file name. For example `folder/image.png`.
     */
    suspend fun download(path: String): ByteArray {
        val finalPath = this.getFinalPath(path)
        val res = storageHttpClient.get(path = "/object/${finalPath}", FetchOptions(headersOf(), true))
        return res.toByteArray()
    }

    /**
     * Retrieve URLs for assets in public buckets
     *
     * @param path The file path to be downloaded, including the path and file name. For example `folder/image.png`.
     */
    fun getPublicUrl(path: String): String {
        val _path = this.getFinalPath(path)
        return "${this.storageHttpClient.url}/object/public/${_path}"
    }

    /**
     * Deletes files within the same bucket
     *
     * @param paths An array of files to be deletes, including the path and file name. For example [`folder/image.png`].
     */
    // suspend fun remove(paths: List<String>): List<FileObject> {
    //    val result = storageHttpClient.remove(
    //        path = "/object/${this.bucketId}",
    //        files = paths,
    //        options = FetchOptions(headers, true)
    //    )
    //    return deserialize(result)
    //}

    /**
     * Get file metadata
     * @param id the file id to retrieve metadata
     */
    // async getMetadata(id: String): Promise<{ data: Metadata | null; error: Error | null }> {
    //   try {
    //     val data = await get(`${this.url}/metadata/${id}`, { headers: this.headers })
    //     return { data, error: null }
    //   } catch (error) {
    //     return { data: null, error }
    //   }
    // }

    /**
     * Update file metadata
     * @param id the file id to update metadata
     * @param meta the new file metadata
     */
    // async updateMetadata(
    //   id: String,
    //   meta: Metadata
    // ): Promise<{ data: Metadata | null; error: Error | null }> {
    //   try {
    //     val data = await post(`${this.url}/metadata/${id}`, { ...meta }, { headers: this.headers })
    //     return { data, error: null }
    //   } catch (error) {
    //     return { data: null, error }
    //   }
    // }

    /**
     * Lists all the files within a bucket.
     * @param path The folder path.
     * @param options Search options, including `limit`, `offset`, and `sortBy`.
     * @param parameters Fetch parameters, currently only supports `signal`, which is an AbortController's signal
     */
    suspend fun list(
        path: String?,
        options: SearchOptions?
    ): List<FileObject> {
        // TODO See of to include prefix
        // val body = { ...DEFAULT_SEARCH_OPTIONS, ...options, prefix: path || '' }
        val body = SearchOptions(
            limit = options?.limit ?: DEFAULT_SEARCH_OPTIONS.limit,
            offset = options?.offset ?: DEFAULT_SEARCH_OPTIONS.offset,
            sortBy = options?.sortBy ?: DEFAULT_SEARCH_OPTIONS.sortBy
        )
        // TODO See why path is not used

        val result = storageHttpClient.post(
            path = "/object/list/${this.bucketId}", // TODO See if it is possible to append URL path with path parameter
            body = body,
            options = null
        )
        return deserialize(result)
    }

    private fun getFinalPath(path: String): String {
        return "${this.bucketId}/${path}"
    }
}
