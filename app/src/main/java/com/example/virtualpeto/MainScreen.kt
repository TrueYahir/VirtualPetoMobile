package com.example.virtualpeto

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import java.io.File

val DarkBackground = Color(0xFF1E1E24)
val SurfaceColor = Color(0xFF2B2B36)
val TextColor = Color(0xFFE0E0E0)
val AccentColor = Color(0xFF8B5CF6)
val OrangeColor = Color(0xFFE67E22)
val RedColor = Color(0xFFE74C3C)

data class LibraryItem(val uri: Uri, val name: String)

class LibraryManager(private val context: Context) {
    private val libraryDir = File(context.filesDir, "library").apply { mkdirs() }

    fun saveFile(uri: Uri): LibraryItem? {
        return try {
            val contentResolver = context.contentResolver
            val fileName = getFileNameFromUri(context, uri)
            val destinationFile = File(libraryDir, fileName)

            contentResolver.openInputStream(uri)?.use { input ->
                destinationFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            LibraryItem(uri = Uri.fromFile(destinationFile), name = fileName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun loadFiles(): List<LibraryItem> {
        val files = libraryDir.listFiles() ?: return emptyList()
        return files.map { file ->
            LibraryItem(uri = Uri.fromFile(file), name = file.name)
        }
    }

    fun deleteFile(fileName: String): Boolean {
        val file = File(libraryDir, fileName)
        return if (file.exists()) file.delete() else false
    }
}

fun getFileNameFromUri(context: Context, uri: Uri): String {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        } finally {
            cursor?.close()
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result?.lastIndexOf('/') ?: -1
        if (cut != -1) {
            result = result?.substring(cut + 1)
        }
    }
    return result ?: "unknown_file"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onSpawnClick: (LibraryItem) -> Unit,
    onClosePetClick: (LibraryItem) -> Unit
) {
    val context = LocalContext.current

    val libraryManager = remember { LibraryManager(context) }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var items by remember { mutableStateOf<List<LibraryItem>>(emptyList()) }
    var selectedFile by remember { mutableStateOf<LibraryItem?>(null) }
    var activePets by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(Unit) {
        items = libraryManager.loadFiles()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val savedItem = libraryManager.saveFile(it)
            if (savedItem != null) {
                if (items.none { existingItem -> existingItem.name == savedItem.name }) {
                    items = items + savedItem
                }
            }
        }
    }

    if (selectedFile != null) {
        BackHandler {
            selectedFile = null
        }
    }

    Scaffold(
        topBar = {
            if (selectedTab == 0) {
                TopAppBar(
                    title = { Text("General Library", color = TextColor) },
                    actions = {
                        IconButton(onClick = {
                            filePickerLauncher.launch(
                                arrayOf(
                                    "image/png",
                                    "image/jpeg",
                                    "image/gif",
                                    "image/webp",
                                    "audio/mpeg"
                                )
                            )
                        }) {
                            Icon(Icons.Filled.Add, contentDescription = null, tint = TextColor)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
                )
            } else if (selectedTab == 4) {
                TopAppBar(
                    title = { Text("Settings", color = TextColor) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceColor)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceColor,
                contentColor = TextColor
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Filled.Folder, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentColor,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentColor,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentColor,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Filled.Build, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentColor,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 4,
                    onClick = { selectedTab = 4 },
                    icon = { Icon(Icons.Filled.Settings, contentDescription = null) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentColor,
                        unselectedIconColor = Color.Gray,
                        indicatorColor = Color.Transparent
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(paddingValues)
        ) {
            if (selectedTab == 0) {
                LibraryGrid(
                    items = items,
                    imageLoader = imageLoader,
                    onItemClick = { clickedItem ->
                        selectedFile = clickedItem
                    }
                )
            } else if (selectedTab == 4) {
                SettingsScreen()
            }

            if (selectedFile != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .clickable { selectedFile = null }
                )
            }

            AnimatedVisibility(
                visible = selectedFile != null,
                enter = slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ),
                exit = slideOutHorizontally(
                    targetOffsetX = { fullWidth -> fullWidth },
                    animationSpec = tween(300)
                ),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                selectedFile?.let { item ->
                    val isActive = activePets.contains(item.name)
                    SidePanelContent(
                        item = item,
                        imageLoader = imageLoader,
                        isActive = isActive,
                        onClose = { selectedFile = null },
                        onLaunch = {
                            activePets = activePets + item.name
                            onSpawnClick(item)
                            selectedFile = null
                        },
                        onClosePet = {
                            activePets = activePets - item.name
                            onClosePetClick(item)
                            selectedFile = null
                        },
                        onDelete = {
                            if (libraryManager.deleteFile(item.name)) {
                                items = items.filter { it.name != item.name }
                            }
                            activePets = activePets - item.name
                            onClosePetClick(item)
                            selectedFile = null
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LibraryGrid(
    items: List<LibraryItem>,
    imageLoader: ImageLoader,
    onItemClick: (LibraryItem) -> Unit
) {
    val context = LocalContext.current
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        contentPadding = PaddingValues(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(SurfaceColor)
                    .clickable { onItemClick(item) }
                    .padding(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .background(DarkBackground)
                        .clip(RoundedCornerShape(4.dp))
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(item.uri)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.name,
                    color = TextColor,
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun SidePanelContent(
    item: LibraryItem,
    imageLoader: ImageLoader,
    isActive: Boolean,
    onClose: () -> Unit,
    onLaunch: () -> Unit,
    onClosePet: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth(0.75f)
            .background(SurfaceColor)
            .pointerInput(Unit) {
                detectHorizontalDragGestures { _, dragAmount ->
                    if (dragAmount > 20) {
                        onClose()
                    }
                }
            }
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkBackground),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.uri)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Name of the archive:",
            color = Color.LightGray,
            fontSize = 12.sp,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = item.name,
            color = TextColor,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = onLaunch,
                enabled = !isActive,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentColor,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (isActive) "Pet is Active" else "Launch Pet")
            }

            Button(
                onClick = onClosePet,
                enabled = isActive,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangeColor,
                    disabledContainerColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Close Active Pet")
            }

            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = RedColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Delete")
            }
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("virtual_pet_prefs", Context.MODE_PRIVATE) }

    var useDefaultFolders by remember {
        mutableStateOf(prefs.getBoolean("use_default_folders", true))
    }
    var customFolderPath by remember {
        mutableStateOf(prefs.getString("custom_folder_path", "No folder selected"))
    }

    LaunchedEffect(Unit) {
        if (useDefaultFolders) {
            File(context.filesDir, "library").mkdirs()
            File(context.filesDir, "pets").mkdirs()
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            customFolderPath = it.toString()
            prefs.edit().putString("custom_folder_path", customFolderPath).apply()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "Use default internal folders", color = TextColor, fontSize = 16.sp)
            Switch(
                checked = useDefaultFolders,
                onCheckedChange = { isChecked ->
                    useDefaultFolders = isChecked
                    prefs.edit().putBoolean("use_default_folders", isChecked).apply()
                    if (isChecked) {
                        File(context.filesDir, "library").mkdirs()
                        File(context.filesDir, "pets").mkdirs()
                    }
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AccentColor,
                    checkedTrackColor = SurfaceColor,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = DarkBackground
                )
            )
        }

        AnimatedVisibility(visible = !useDefaultFolders) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(text = "Custom Folder:", color = Color.LightGray, fontSize = 14.sp)
                Text(text = customFolderPath ?: "", color = TextColor, fontSize = 12.sp)
                Button(
                    onClick = { launcher.launch(null) },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentColor)
                ) {
                    Text("Select Folder")
                }
            }
        }
    }
}