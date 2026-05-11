package com.xapps.question_generator.infrastructure.object_store

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.InputStream
import java.time.Instant
import java.util.UUID

@Service
class R2ObjectStore(
    private val s3: S3Client
) : ObjectStore {

    override suspend fun put(
        bucket: String,
        file: MultipartFile
    ): PutResult {

        return putStream(
            bucket = bucket,
            fileName = file.originalFilename ?: file.name,
            mimeType = file.contentType ?: "application/octet-stream",
            stream = file.inputStream,
            knownSize = file.size
        )
    }

    override suspend fun put(
        bucket: String,
        fileName: String,
        mimeType: String,
        bytes: ByteArray
    ): PutResult {

        return withContext(Dispatchers.IO) {

            val key = UUID.randomUUID().toString().take(36)

            s3.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(mimeType)
                    .build(),
                RequestBody.fromBytes(bytes)
            )

            PutResult(
                key = key,
                metadata = ObjectMetadata(
                    key = key,
                    originalFilename = fileName,
                    contentType = mimeType,
                    sizeBytes = bytes.size.toLong(),
                    uploadedAt = Instant.now(),
                    storagePath = key
                )
            )
        }
    }

    override suspend fun putStream(
        bucket: String,
        fileName: String,
        mimeType: String,
        stream: InputStream,
        knownSize: Long
    ): PutResult {

        return withContext(Dispatchers.IO) {

            val key = UUID.randomUUID().toString()

            s3.putObject(
                PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(mimeType)
                    .build(),
                RequestBody.fromInputStream(stream, knownSize)
            )

            PutResult(
                key = key,
                metadata = ObjectMetadata(
                    key = key,
                    originalFilename = fileName,
                    contentType = mimeType,
                    sizeBytes = knownSize,
                    uploadedAt = Instant.now(),
                    storagePath = key
                )
            )
        }
    }

    override suspend fun delete(
        bucket: String,
        key: ObjectKey
    ): Boolean {

        return withContext(Dispatchers.IO) {

            try {

                s3.deleteObject(
                    DeleteObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
                )

                true

            } catch (_: NoSuchKeyException) {
                false
            }
        }
    }

    override fun get(
        key: ObjectKey,
        bucket: String
    ): ObjectHandle? {

        val metadata = head(
            bucket = bucket,
            key = key
        ) ?: return null

        return ObjectHandle(
            metadata = metadata,
            streamProvider = {

                s3.getObject(
                    GetObjectRequest.builder()
                        .bucket(bucket)
                        .key(key)
                        .build()
                )
            }
        )
    }

    override fun head(
        bucket: String,
        key: ObjectKey
    ): ObjectMetadata? {

        return try {

            val response = s3.headObject(
                HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build()
            )

            ObjectMetadata(
                key = key,
                originalFilename = key,
                contentType = response.contentType(),
                sizeBytes = response.contentLength(),
                uploadedAt = response.lastModified(),
                storagePath = key
            )

        } catch (_: NoSuchKeyException) {
            null
        }
    }
}