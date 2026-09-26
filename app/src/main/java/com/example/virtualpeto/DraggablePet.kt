package com.example.virtualpeto

import android.os.Build.VERSION.SDK_INT
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest

@Composable
fun DraggablePet(
    petState: PetState,
    onDrag: (Float, Float) -> Unit,
    onDragStart: () -> Unit,
    onDragEnd: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageLoader = ImageLoader.Builder(context)
        .components {
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()

    val imageRes = when (petState) {
        PetState.IDLE -> R.drawable.sample_pet
        PetState.WALK_LEFT -> R.drawable.sample_pet
        PetState.WALK_RIGHT -> R.drawable.sample_pet
        PetState.CLIMB_LEFT -> R.drawable.sample_pet
        PetState.CLIMB_RIGHT -> R.drawable.sample_pet
        PetState.SLEEP -> R.drawable.sample_pet
        PetState.FALLING -> R.drawable.sample_pet
        PetState.DRAGGED -> R.drawable.sample_pet
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(imageRes)
            .build(),
        imageLoader = imageLoader
    )

    Box(
        modifier = Modifier
            .width(100.dp)
            .height(140.dp)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onClick() }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { onDragStart() },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDrag(dragAmount.x, dragAmount.y)
                    }
                )
            }
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.BottomCenter)
        )
    }
}