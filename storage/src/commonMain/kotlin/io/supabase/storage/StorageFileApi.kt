package io.supabase.storage

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.utils.io.core.*
import io.supabase.storage.http.bodies.FileObjectRemoveBody
import io.supabase.storage.http.bodies.SignedUrlBody
import io.supabase.storage.http.responses.FileObjectResult
import io.supabase.storage.types.FileObject
import io.supabase.storage.types.FileOptions
import io.supabase.storage.types.SearchOptions

val DEFAULT_FILE_OPTIONS = FileOptions(
    cacheControl = "3600",
    contentType = "text/plain;charset=UTF-8",
    upsert = false
)

class StorageFileApi(
    private val bucketId: String,
    private val url: String,
    private val headers: Headers,
    private val httpClient: () -> HttpClient
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

        val requestBody: Any = when (fileBody) {
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
            httpClient().post("$url/object/$finalPath") {
                headers {
                    appendAll(this@StorageFileApi.headers)
                }
                setBody(requestBody)
            }
        } else {
            httpClient().put("$url/object/$finalPath") {
                headers {
                    appendAll(this@StorageFileApi.headers)
                }
                setBody(requestBody)
            }
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
        return httpClient().post("$url/object/move") {
            headers {
                appendAll(this@StorageFileApi.headers)
            }
            setBody(formData {
                append("bucketId", bucketId)
                append("sourceKey", fromPath)
                append("destinationKey", toPath)
            })
        }.body()
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
        val data: String = httpClient().post("$url/object/sign/$finalPath") {
            headers {
                appendAll(this@StorageFileApi.headers)
            }
            setBody(SignedUrlBody(expiresIn))
        }.body()
        return "$url$data" // Signed URL
    }

    /**
     * Downloads a file.
     *
     * @param path The file path to be downloaded, including the path and file name. For example `folder/image.png`.
     */
    suspend fun download(path: String): ByteArray {
        val finalPath = this.getFinalPath(path)
        val response: HttpResponse = httpClient().get("$url/object/$finalPath") {
            headers {
                appendAll(this@StorageFileApi.headers)
            }
        }
        return response.body()
    }

    /**
     * Retrieve URLs for assets in public buckets
     *
     * @param path The file path to be downloaded, including the path and file name. For example `folder/image.png`.
     */
    fun getPublicUrl(path: String): String {
        return "$url/object/public/${getFinalPath(path)}"
    }

    /**
     * Deletes files within the same bucket
     *
     * @param paths An array of files to be deletes, including the path and file name. For example [`folder/image.png`].
     */
    suspend fun remove(paths: List<String>): FileObjectResult {
        return try {
            val response: List<FileObject> = httpClient().delete("$url/object/$bucketId") {
                headers {
                    appendAll(this@StorageFileApi.headers)
                }
                setBody(FileObjectRemoveBody(paths))
            }.body()
            FileObjectResult.Success(response)
        } catch (e: Exception) {
            FileObjectResult.Failure(e.message ?: e.toString())
        }
    }

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
     * @param options Search options, including `limit`, `offset`, and `sortBy`.
     */
    suspend fun list(options: SearchOptions = SearchOptions()): FileObjectResult {
        return try {
            val objects: List<FileObject> = httpClient().post("$url/object/list/$bucketId") {
                headers {
                    appendAll(this@StorageFileApi.headers)
                }
                setBody(options)
            }.body()
            FileObjectResult.Success(objects)
        } catch (error: Exception) {
            FileObjectResult.Failure(error.message ?: error.toString())
        }
    }

    private fun getFinalPath(path: String): String {
        return "$bucketId/$path"
    }
}
