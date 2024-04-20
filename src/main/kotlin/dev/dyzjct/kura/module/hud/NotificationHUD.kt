package dev.dyzjct.kura.module.hud

import base.notification.NotificationManager
import base.system.render.graphic.Render2DEngine
import base.system.render.newfont.FontRenderers
import dev.dyzjct.kura.module.HUDModule
import net.minecraft.client.gui.DrawContext
import java.awt.Color

object NotificationHUD : HUDModule(
    name = "NotificationNew", langName = "通知界面", x = 300f, y = 150f
) {
    private val mode by msetting("Mode", NoticeMode.Normal)
    private val interval by isetting("Interval", 0, 0, 20)
    val animationLength by isetting("AnimationLength", 15, 10, 100)
    private val color by csetting("Color", Color(0, 0, 0))
    override fun onRender(context: DrawContext) {
        width = 150f
        height = 15f
        if (NotificationManager.taskList.isEmpty()) return
        try {
            var count = 1f
            for (notification in NotificationManager.taskList) {
                if (notification.reversed && (notification.animation >= 1f)) {
                    count += 1f.symbolArranged(
                        !notification.reversed,
                        notification.animation
                    )
                    NotificationManager.taskList.remove(notification)
                    continue
                }
                width =
                    FontRenderers.cn.getStringWidth(notification.message) + 6 + if (mode == NoticeMode.SanLiuLing) 12 else 0
                val offsetX = x + 150 - width
                val animationXOffset = offsetX + width * notification.animation
                val arrangedHeight = if (y < mc.window.scaledHeight / 2)
                    (height + 5 + interval) * count
                        .symbolArranged(
                            !notification.reversed,
                            notification.animation
                        ) else -(height + 5 + interval) * count
                    .symbolArranged(!notification.reversed, notification.animation)
                when (mode) {
                    NoticeMode.Normal -> {
                        Render2DEngine.drawRect(
                            context.matrices,
                            animationXOffset,
                            y + arrangedHeight,
                            width,
                            height,
                            color
                        )

                        FontRenderers.cn.drawString(
                            context.matrices,
                            notification.message,
                            animationXOffset + 3,
                            y.symbolArranged(true, height / 2f) + arrangedHeight - 1.5f,
                            Color(255, 255, 255).rgb
                        )
                    }

                    NoticeMode.SanLiuLing -> {
                        Render2DEngine.drawRect(
                            context.matrices,
                            animationXOffset,
                            y + arrangedHeight,
                            width,
                            height,
                            color
                        )
                        Render2DEngine.drawRect(
                            context.matrices,
                            animationXOffset,
                            y + arrangedHeight,
                            width * notification.animation,
                            height / 10,
                            Color(130, 255, 120, color.alpha)
                        )
                        FontRenderers.cn.drawString(
                            context.matrices,
                            notification.message,
                            animationXOffset + 3,
                            y.symbolArranged(true, height / 2f) + arrangedHeight - 1.5f,
                            Color(255, 255, 255).rgb
                        )
                    }
                }
                count += 1f.symbolArranged(
                    !notification.reversed,
                    notification.animation
                )
            }
        } catch (e: Exception) {
            println(e)
        }
    }

    private fun Float.symbolArranged(left: Boolean, numberCalc: Float): Float {
        return if (left) {
            plus(numberCalc)
        } else {
            minus(numberCalc)
        }
    }

    override fun onEnable() {
        NotificationManager.taskList.clear()
    }

    override fun onDisable() {
        NotificationManager.taskList.clear()
    }

    enum class NoticeMode {
        Normal, SanLiuLing
    }
}