package com.example.virtualpeto

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

class PetService : LifecycleService(), SavedStateRegistryOwner, ViewModelStoreOwner {

    private lateinit var windowManager: WindowManager
    private lateinit var composeView: ComposeView
    private lateinit var layoutParams: WindowManager.LayoutParams

    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    private var screenWidth = 0
    private var screenHeight = 0
    private var petHeightPx = 0
    private var petWidthPx = 0

    private var exactX = 100f
    private var exactY = 100f
    private var targetWalkX = 0f

    private var currentState by mutableStateOf(PetState.FALLING)

    private var gameLoopJob: Job? = null
    private var aiDecisionJob: Job? = null

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val viewModelStore: ViewModelStore
        get() = store

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        startForegroundNotification()
        calculateScreenMetrics()
        setupOverlay()
        startGameLoop()
    }

    private fun calculateScreenMetrics() {
        val metrics = resources.displayMetrics
        screenWidth = metrics.widthPixels
        screenHeight = metrics.heightPixels
        petHeightPx = (140 * metrics.density).toInt()
        petWidthPx = (100 * metrics.density).toInt()
    }

    private fun startForegroundNotification() {
        val channelId = "pet_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pet Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Pet Active")
            .setSmallIcon(android.R.drawable.ic_menu_myplaces)
            .build()

        startForeground(1, notification)
    }

    private fun setupOverlay() {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = exactX.toInt()
            y = exactY.toInt()
        }

        composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(this@PetService)
            setViewTreeSavedStateRegistryOwner(this@PetService)
            setViewTreeViewModelStoreOwner(this@PetService)

            setContent {
                DraggablePet(
                    petState = currentState,
                    onDragStart = {
                        currentState = PetState.DRAGGED
                        aiDecisionJob?.cancel()
                    },
                    onDrag = { dx, dy ->
                        exactX += dx
                        exactY += dy
                        this@PetService.layoutParams.x = exactX.toInt()
                        this@PetService.layoutParams.y = exactY.toInt()
                        windowManager.updateViewLayout(this@apply, this@PetService.layoutParams)
                    },
                    onDragEnd = {
                        currentState = PetState.FALLING
                    },
                    onClick = {
                        Toast.makeText(this@PetService, "Menú de la mascota abierto", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        windowManager.addView(composeView, layoutParams)
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = lifecycleScope.launch {
            val visualBottomOffset = 40 * resources.displayMetrics.density
            val floorY = screenHeight - petHeightPx.toFloat() - visualBottomOffset
            val speed = 3f

            while (true) {
                var positionChanged = false

                when (currentState) {
                    PetState.FALLING -> {
                        exactY += 15f
                        if (exactY >= floorY) {
                            exactY = floorY
                            changeState(PetState.IDLE)
                        }
                        positionChanged = true
                    }
                    PetState.WALK_LEFT -> {
                        exactX -= speed
                        if (exactX <= targetWalkX) {
                            exactX = targetWalkX
                            changeState(PetState.IDLE)
                        }
                        positionChanged = true
                    }
                    PetState.WALK_RIGHT -> {
                        exactX += speed
                        if (exactX >= targetWalkX) {
                            exactX = targetWalkX
                            changeState(PetState.IDLE)
                        }
                        positionChanged = true
                    }
                    else -> {
                    }
                }

                if (positionChanged) {
                    layoutParams.x = exactX.toInt()
                    layoutParams.y = exactY.toInt()
                    windowManager.updateViewLayout(composeView, layoutParams)
                }

                delay(16)
            }
        }
    }

    private fun changeState(newState: PetState) {
        if (currentState == newState) return
        currentState = newState

        aiDecisionJob?.cancel()

        when (newState) {
            PetState.IDLE -> {
                aiDecisionJob = lifecycleScope.launch {
                    val idleTime = Random.nextLong(2000, 5000)
                    delay(idleTime)

                    val nextMove = if (Random.nextBoolean()) PetState.WALK_LEFT else PetState.WALK_RIGHT
                    changeState(nextMove)
                }
            }
            PetState.WALK_LEFT -> {
                val distance = Random.nextInt(50, screenWidth).toFloat()
                targetWalkX = maxOf(0f, exactX - distance)
            }
            PetState.WALK_RIGHT -> {
                val maxX = screenWidth - petWidthPx.toFloat()
                val distance = Random.nextInt(50, screenWidth).toFloat()
                targetWalkX = minOf(maxX, exactX + distance)
            }
            else -> {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        gameLoopJob?.cancel()
        aiDecisionJob?.cancel()
        if (::composeView.isInitialized) {
            windowManager.removeView(composeView)
        }
    }
}