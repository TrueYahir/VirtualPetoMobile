data class AnimationData(
    var isSpriteSheet: Boolean = false,
    var frameWidth: Int = 64,
    var frameHeight: Int = 64,
    var columns: Int = 1,
    var rows: Int = 1,
    var totalFrames: Int = 1,
    var fps: Int = 10,
    var soundPath: String = "",
    var imagePath: String = ""
)

data class PetAnimState(
    val name: String,
    val type: String,
    var data: AnimationData
)

data class PetMetadata(
    var petName: String = "",
    var author: String = "",
    var isSmartPet: Boolean = true,
    var animations: List<PetAnimState> = listOf(
        PetAnimState("IDLE", "Base", AnimationData()),
        PetAnimState("SLEEP", "Base", AnimationData()),
        PetAnimState("WALK RIGHT", "Mov", AnimationData()),
        PetAnimState("WALK LEFT", "Mov", AnimationData()),
        PetAnimState("RUN RIGHT", "Mov", AnimationData()),
        PetAnimState("RUN LEFT", "Mov", AnimationData())
    )
)