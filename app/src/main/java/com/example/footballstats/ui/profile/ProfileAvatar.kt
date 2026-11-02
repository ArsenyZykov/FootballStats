package com.example.footballstats.ui.profile

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ProfileAvatar(uri: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val avatar by produceState<ImageBitmap?>(initialValue = null, key1 = uri) {
        value = null
        if (uri.isNotBlank()) {
            value = withContext(Dispatchers.IO) {
                try {
                    context.contentResolver.openInputStream(uri.toUri())?.use {
                        BitmapFactory.decodeStream(it)?.asImageBitmap()
                    }
                } catch (_: Exception) { null }
            }
        }
    }
    Surface(
        modifier = modifier.size(112.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Box(contentAlignment = Alignment.Center) {
            avatar?.let {
                Image(it, contentDescription = "Фото профиля", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } ?: Icon(Icons.Default.Person, contentDescription = "Фото не выбрано", modifier = Modifier.size(64.dp))
        }
    }
}
