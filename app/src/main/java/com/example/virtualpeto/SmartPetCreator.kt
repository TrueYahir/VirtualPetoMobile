package com.example.virtualpeto

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.io.Serializable

data class PetMetadata(
    val petName: String = "",
    val author: String = "",
    val animations: List<PetAnimState> = emptyList()
) : Serializable

data class PetAnimState(
    val name: String,
    val type: String,
    val data: AnimationData
) : Serializable

data class AnimationData(
    val imagePath: String = "",
    val soundPath: String = "",
    val isSpriteSheet: Boolean = false,
    val frameWidth: Int = 0,
    val frameHeight: Int = 0,
    val columns: Int = 0,
    val rows: Int = 0,
    val totalFrames: Int = 0,
    val fps: Int = 12
) : Serializable

// Función para generar las animaciones predeterminadas de cada categoría
fun getDefaultAnimations(): List<PetAnimState> {
    return listOf(
        PetAnimState("IDLE", "Base", AnimationData()),
        PetAnimState("SLEEP", "Base", AnimationData()),
        PetAnimState("WALK", "Mov", AnimationData()),
        PetAnimState("RUN", "Mov", AnimationData()),
        PetAnimState("HAPPY", "Spe", AnimationData()),
        PetAnimState("HUNGRY", "Spe", AnimationData()),
        PetAnimState("EAT", "Item", AnimationData())
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PetCreatorScreen(onClose: () -> Unit) {
    val context = LocalContext.current

    // Inicializar metadata con las animaciones por defecto
    var metadata by remember { mutableStateOf(PetMetadata(animations = getDefaultAnimations())) }

    var selectedAnim by remember { mutableStateOf<PetAnimState?>(null) }
    var animToDelete by remember { mutableStateOf<PetAnimState?>(null) }
    var selectedCategory by remember { mutableStateOf("Base") }

    val categories = listOf("Base", "Mov", "Spe", "Item", "Random")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1E24))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Pet Name:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = metadata.petName,
                    onValueChange = { metadata = metadata.copy(petName = it) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2B2B36),
                        unfocusedContainerColor = Color(0xFF2B2B36),
                        focusedBorderColor = Color(0xFF9074FF),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Author / Creator:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                OutlinedTextField(
                    value = metadata.author,
                    onValueChange = { metadata = metadata.copy(author = it) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2B2B36),
                        unfocusedContainerColor = Color(0xFF2B2B36),
                        focusedBorderColor = Color(0xFF9074FF),
                        unfocusedBorderColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                )
            }

            IconButton(
                onClick = {},
                modifier = Modifier.padding(start = 8.dp, top = 24.dp)
            ) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            categories.forEach { category ->
                Text(
                    text = category,
                    color = if (selectedCategory == category) Color(0xFF9074FF) else Color.Gray,
                    fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable { selectedCategory = category }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val currentAnimations = metadata.animations.filter { it.type == selectedCategory }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(currentAnimations) { anim ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { selectedAnim = anim },
                            onLongClick = { animToDelete = anim }
                        ),
                    color = Color(0xFF2B2B36),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF383846))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF1E1E24), RoundedCornerShape(4.dp))
                                .border(1.dp, Color.Gray, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (anim.data.imagePath.isNotEmpty()) {
                                AsyncImage(
                                    model = anim.data.imagePath,
                                    contentDescription = "Anim Image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp))
                                )
                            } else {
                                Text("IMG", color = Color.Gray, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = anim.name.uppercase(),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val displayPath = anim.data.imagePath.takeIf { it.isNotEmpty() }
                                ?: anim.data.soundPath.takeIf { it.isNotEmpty() }
                                ?: "Ruta"
                            Text(
                                text = displayPath,
                                color = Color.Gray,
                                fontSize = 14.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Restringir el botón de añadir solo para la pestaña "Random" como estaba originalmente
            if (selectedCategory == "Random") {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val newName = "Anim_${metadata.animations.size + 1}"
                                val newAnim = PetAnimState(newName, selectedCategory, AnimationData())
                                metadata = metadata.copy(animations = metadata.animations + newAnim)
                                selectedAnim = newAnim
                            },
                        color = Color(0xFF2B2B36),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF383846))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(Color(0xFF1E1E24), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            )

                            Spacer(modifier = Modifier.width(16.dp))

                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add",
                                tint = Color(0xFF9074FF),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Add Animation", color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (metadata.petName.isBlank()) {
                    Toast.makeText(context, "Provide a name", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                try {
                    val petsDir = File(context.filesDir, "pets")
                    petsDir.mkdirs()

                    val file = File(petsDir, "${metadata.petName.replace(" ", "_")}.vpet")
                    ObjectOutputStream(FileOutputStream(file)).use { it.writeObject(metadata) }

                    Toast.makeText(context, "Smart Pet Compiled!", Toast.LENGTH_SHORT).show()

                    onClose()
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error compiling pet", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9074FF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Compile Smart Pet (.vpet)", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }

    animToDelete?.let { anim ->
        AlertDialog(
            onDismissRequest = { animToDelete = null },
            title = { Text("Delete Animation", color = Color.White) },
            text = { Text("Are you sure you want to delete the animation '${anim.name}'?", color = Color.LightGray) },
            confirmButton = {
                TextButton(onClick = {
                    metadata = metadata.copy(animations = metadata.animations - anim)
                    animToDelete = null
                }) {
                    Text("Delete", color = Color(0xFFFF4C4C), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { animToDelete = null }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = Color(0xFF2B2B36)
        )
    }

    selectedAnim?.let { anim ->
        AnimationSettingsDialog(
            initialAnim = anim,
            onDismiss = { selectedAnim = null },
            onSave = { updatedAnim ->
                val updatedList = metadata.animations.map {
                    if (it == anim) updatedAnim else it
                }
                metadata = metadata.copy(animations = updatedList)
                selectedAnim = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimationSettingsDialog(
    initialAnim: PetAnimState,
    onDismiss: () -> Unit,
    onSave: (PetAnimState) -> Unit
) {
    var animName by remember { mutableStateOf(initialAnim.name) }
    var data by remember { mutableStateOf(initialAnim.data.copy()) }
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            data = data.copy(imagePath = it.toString())
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            data = data.copy(soundPath = it.toString())
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF1E1E24)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    "Animation Settings",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                OutlinedTextField(
                    value = animName,
                    onValueChange = { animName = it },
                    label = { Text("Animation Name (e.g. IDLE, WALK)", color = Color.Gray) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2B2B36),
                        unfocusedContainerColor = Color(0xFF2B2B36),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color(0xFF12121A), RoundedCornerShape(8.dp))
                        .clickable { imagePickerLauncher.launch(arrayOf("image/*", "video/*")) }
                        .clip(RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    if (data.imagePath.isNotEmpty()) {
                        AsyncImage(
                            model = data.imagePath,
                            contentDescription = "Preview Image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Text("Tap to add Image / GIF", color = Color.Gray)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = data.isSpriteSheet,
                        onCheckedChange = { data = data.copy(isSpriteSheet = it) },
                        colors = CheckboxDefaults.colors(checkedColor = Color(0xFF9074FF))
                    )
                    Text("Use custom dimensions (Sprite Sheet)", color = Color.White)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    NumericField("Frame Width", data.frameWidth.toString(), Modifier.weight(1f)) { data = data.copy(frameWidth = it) }
                    NumericField("Frame Height", data.frameHeight.toString(), Modifier.weight(1f)) { data = data.copy(frameHeight = it) }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    NumericField("Columns", data.columns.toString(), Modifier.weight(1f), enabled = data.isSpriteSheet) { data = data.copy(columns = it) }
                    NumericField("Rows", data.rows.toString(), Modifier.weight(1f), enabled = data.isSpriteSheet) { data = data.copy(rows = it) }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                    NumericField("Total Frames", data.totalFrames.toString(), Modifier.weight(1f), enabled = data.isSpriteSheet) { data = data.copy(totalFrames = it) }
                    NumericField("FPS", data.fps.toString(), Modifier.weight(1f)) { data = data.copy(fps = it) }
                }

                Text("Audio File:", color = Color.White)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = data.soundPath,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF2B2B36),
                            unfocusedContainerColor = Color(0xFF2B2B36),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Button(
                        onClick = { audioPickerLauncher.launch(arrayOf("audio/*")) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9074FF))
                    ) {
                        Text("Browse")
                    }
                    Button(
                        onClick = { data = data.copy(soundPath = "") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4C4C))
                    ) {
                        Text("Clear")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Button(
                        onClick = { onSave(initialAnim.copy(name = animName, data = data)) },
                        modifier = Modifier.weight(1f).height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9074FF))
                    ) {
                        Text("Save", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(45.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4C4C))
                    ) {
                        Text("Cancel", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun NumericField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = modifier) {
        Text(label, color = Color.White, fontSize = 12.sp)
        OutlinedTextField(
            value = value,
            onValueChange = {
                it.toIntOrNull()?.let { num -> onValueChange(num) }
            },
            enabled = enabled,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2B2B36),
                unfocusedContainerColor = Color(0xFF2B2B36),
                disabledContainerColor = Color(0xFF2B2B36).copy(alpha = 0.5f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )
    }
}