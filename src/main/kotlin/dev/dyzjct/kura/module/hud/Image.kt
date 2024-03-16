package dev.dyzjct.kura.module.hud

import base.KuraIdentifier
import base.utils.chat.ChatUtil
import com.mojang.blaze3d.systems.RenderSystem
import dev.dyzjct.kura.gui.clickgui.ClickGuiScreen
import dev.dyzjct.kura.gui.clickgui.HudEditorScreen
import dev.dyzjct.kura.module.Category
import dev.dyzjct.kura.module.HUDModule
import dev.dyzjct.kura.module.modules.client.UiSetting
import dev.dyzjct.kura.utils.animations.Easing
import net.minecraft.client.gui.DrawContext

object Image : HUDModule(name = "Image", langName = "图片显示", category = Category.HUD, x = 150f, y = 150f) {
    val mode = msetting("Mode", Mode.Rimuru)
    private val scale by fsetting("Scale", 1.0f, 0.0f, 2.0f)
    private val rimuru = KuraIdentifier("textures/rimuru.png")
    private val arona = KuraIdentifier("textures/arona.png")
    private val mahiro = KuraIdentifier("textures/mahiro.png")
    private val roxy = KuraIdentifier("textures/roxy.png")
    var startTime = System.currentTimeMillis()
    override fun onRender(context: DrawContext) {
        val img = when (mode.value) {
            Mode.Rimuru -> rimuru
            Mode.Arona -> arona
            Mode.Roxy -> roxy
            else -> mahiro
        }
        try {
            if (mc.currentScreen is ClickGuiScreen || mc.currentScreen is HudEditorScreen) {
                RenderSystem.disableBlend()
                width = 302f * scale
                height = 460f * scale
                val progress = Easing.IN_QUAD.inc(Easing.toDelta(startTime, UiSetting.animationLength))
                context.drawTexture(
                    img,
                    (x + 302f - (302f * progress)).toInt(),
                    (y + 460f - (460f * progress)).toInt(),
                    0F,
                    0F,
                    (302f * progress * scale).toInt(),
                    (460f * progress * scale).toInt(),
                    (302 * progress * scale).toInt(),
                    (460 * progress * scale).toInt()
                )
                RenderSystem.enableBlend()
            }
        } catch (e: Exception) {
            ChatUtil.sendNoSpamMessage("Image failed!!")
        }
    }

    @Suppress("UNUSED")
    enum class Mode {
        Rimuru, Arona, Mahiro, Roxy
    }
}