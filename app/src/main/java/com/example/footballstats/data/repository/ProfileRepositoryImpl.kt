package com.example.footballstats.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.exifinterface.media.ExifInterface
import com.example.footballstats.domain.model.ProfileDocument
import com.example.footballstats.domain.model.UserProfile
import com.example.footballstats.domain.repository.ProfileRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID
import java.util.concurrent.TimeUnit

private val Context.profileDataStore by preferencesDataStore(
    name = "user_profile"
)

class ProfileRepositoryImpl(context: Context) : ProfileRepository {

    private val context = context.applicationContext
    private val dataStore = this.context.profileDataStore
    private val authority = "${this.context.packageName}.profile.files"

    private val avatarDirectory
        get() = File(context.filesDir, "avatars").apply { mkdirs() }

    private val cameraDirectory
        get() = File(context.cacheDir, "camera").apply { mkdirs() }

    private val resumeDirectory
        get() = File(context.cacheDir, "resumes").apply { mkdirs() }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(60, TimeUnit.SECONDS)
        .followSslRedirects(false)
        .build()

    override val profile = dataStore.data.map { preferences ->
        UserProfile(
            fullName = preferences[NAME].orEmpty(),
            position = preferences[POSITION].orEmpty(),
            avatarUri = preferences[AVATAR].orEmpty(),
            resumeUrl = preferences[RESUME].orEmpty(),
            lessonTime = preferences[LESSON_TIME].orEmpty()
        )
    }.distinctUntilChanged()

    override suspend fun save(profile: UserProfile) =
        withContext(Dispatchers.IO) {
            val previousAvatar =
                this@ProfileRepositoryImpl.profile.first().avatarUri

            dataStore.edit {
                it[NAME] = profile.fullName
                it[POSITION] = profile.position
                it[AVATAR] = profile.avatarUri
                it[RESUME] = profile.resumeUrl
                it[LESSON_TIME] = profile.lessonTime
            }

            if (previousAvatar != profile.avatarUri) {
                deleteOwnedPhoto(previousAvatar)
            }
        }

    override suspend fun createCameraPhoto(): String =
        withContext(Dispatchers.IO) {
            contentUri(
                File.createTempFile("camera_", ".jpg", cameraDirectory)
            )
        }

    override suspend fun importAvatar(sourceUri: String): String =
        withContext(Dispatchers.IO) {
            val source = File.createTempFile(
                "import_",
                ".tmp",
                cameraDirectory
            )
            val target = File(
                avatarDirectory,
                "${UUID.randomUUID()}.jpg"
            )

            try {
                val input = context.contentResolver
                    .openInputStream(sourceUri.toUri())
                    ?: throw IOException("Не удалось открыть фото")

                input.use { stream ->
                    source.outputStream().use {
                        copyLimited(stream, it)
                    }
                }

                val bounds = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                BitmapFactory.decodeFile(source.path, bounds)

                if (bounds.outWidth <= 0 || bounds.outHeight <= 0) {
                    throw IOException("Некорректное фото")
                }

                var sampleSize = 1
                while (
                    maxOf(bounds.outWidth, bounds.outHeight) / sampleSize > 1024
                ) {
                    sampleSize *= 2
                }

                val bitmap = BitmapFactory.decodeFile(
                    source.path,
                    BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                    }
                ) ?: throw IOException("Неподдерживаемое фото")

                try {
                    val exif = ExifInterface(source.path)
                    val matrix = Matrix().apply {
                        when (
                            exif.getAttributeInt(
                                ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_NORMAL
                            )
                        ) {
                            ExifInterface.ORIENTATION_FLIP_HORIZONTAL ->
                                setScale(-1f, 1f)

                            ExifInterface.ORIENTATION_ROTATE_180 ->
                                setRotate(180f)

                            ExifInterface.ORIENTATION_FLIP_VERTICAL ->
                                setScale(1f, -1f)

                            ExifInterface.ORIENTATION_TRANSPOSE -> {
                                setRotate(90f)
                                postScale(-1f, 1f)
                            }

                            ExifInterface.ORIENTATION_ROTATE_90 ->
                                setRotate(90f)

                            ExifInterface.ORIENTATION_TRANSVERSE -> {
                                setRotate(270f)
                                postScale(-1f, 1f)
                            }

                            ExifInterface.ORIENTATION_ROTATE_270 ->
                                setRotate(270f)
                        }
                    }

                    val oriented = Bitmap.createBitmap(
                        bitmap,
                        0,
                        0,
                        bitmap.width,
                        bitmap.height,
                        matrix,
                        true
                    )

                    try {
                        target.outputStream().use {
                            if (
                                !oriented.compress(
                                    Bitmap.CompressFormat.JPEG,
                                    90,
                                    it
                                )
                            ) {
                                throw IOException(
                                    "Не удалось сохранить фото"
                                )
                            }
                        }
                    } finally {
                        if (oriented !== bitmap) {
                            oriented.recycle()
                        }
                    }
                } finally {
                    bitmap.recycle()
                }

                contentUri(target)
            } catch (error: Exception) {
                target.delete()
                throw error
            } finally {
                source.delete()
            }
        }

    override suspend fun discardPhoto(uri: String) =
        withContext(Dispatchers.IO) {
            if (uri != profile.first().avatarUri) {
                deleteOwnedPhoto(uri)
            }
        }

    private fun deleteOwnedPhoto(value: String) {
        val uri = value.toUri()
        if (uri.authority != authority) return

        val segments = uri.pathSegments
        if (segments.size != 2) return

        val directory = when (segments[0]) {
            "avatars" -> avatarDirectory
            "camera" -> cameraDirectory
            else -> return
        }

        val file = File(directory, segments[1])

        if (file.canonicalFile.parentFile == directory.canonicalFile) {
            file.delete()
        }
    }

    override suspend fun downloadResume(url: String): ProfileDocument =
        withContext(Dispatchers.IO) {
            val request = Request.Builder().url(url).build()

            require(request.url.isHttps) {
                "Используйте ссылку HTTPS"
            }

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IOException("HTTP ${response.code}")
                }

                val body = response.body
                    ?: throw IOException("Пустой ответ")

                val type = body.contentType()?.let {
                    "${it.type}/${it.subtype}"
                }

                if (type == "text/html") {
                    throw IOException(
                        "Ссылка ведёт на страницу, а не на файл"
                    )
                }

                if (body.contentLength() > MAX_BYTES) {
                    throw IOException("Файл больше 25 МБ")
                }

                val specificType = type?.takeUnless {
                    it == "application/octet-stream"
                }

                val name = URLUtil.guessFileName(
                    url,
                    response.header("Content-Disposition"),
                    specificType
                )
                    .replace(Regex("[^\\p{L}\\p{N}._-]"), "_")
                    .takeLast(100)
                    .ifBlank { "resume.pdf" }

                val mimeType = specificType
                    ?: MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(
                            name.substringAfterLast('.', "").lowercase()
                        )
                    ?: "application/octet-stream"

                val directory = File(
                    resumeDirectory,
                    UUID.randomUUID().toString()
                )

                if (!directory.mkdirs()) {
                    throw IOException(
                        "Не удалось создать папку загрузки"
                    )
                }

                val target = File(directory, name)

                try {
                    body.byteStream().use { input ->
                        target.outputStream().use {
                            copyLimited(input, it)
                        }
                    }

                    if (target.length() == 0L) {
                        throw IOException("Пустой файл")
                    }

                    ProfileDocument(
                        uri = contentUri(target),
                        mimeType = mimeType
                    )
                } catch (error: Exception) {
                    target.delete()
                    directory.delete()
                    throw error
                }
            }
        }

    private suspend fun copyLimited(
        input: InputStream,
        output: OutputStream
    ) {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        var total = 0L

        while (true) {
            currentCoroutineContext().ensureActive()

            val count = input.read(buffer)
            if (count < 0) break

            total += count

            if (total > MAX_BYTES) {
                throw IOException("Файл больше 25 МБ")
            }

            output.write(buffer, 0, count)
        }
    }

    private fun contentUri(file: File): String =
        FileProvider.getUriForFile(
            context,
            authority,
            file
        ).toString()

    private companion object {
        const val MAX_BYTES = 25L * 1024 * 1024

        val NAME = stringPreferencesKey("full_name")
        val POSITION = stringPreferencesKey("position")
        val AVATAR = stringPreferencesKey("avatar_uri")
        val RESUME = stringPreferencesKey("resume_url")
        val LESSON_TIME = stringPreferencesKey("lesson_time")
    }
}