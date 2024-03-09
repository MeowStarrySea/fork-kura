package dev.dyzjct.kura.manager

import com.mojang.blaze3d.platform.GlStateManager
import dev.dyzjct.kura.gui.clickgui.GUIRender
import dev.dyzjct.kura.gui.clickgui.HUDRender
import dev.dyzjct.kura.gui.clickgui.guis.ClickGuiScreen
import dev.dyzjct.kura.gui.clickgui.guis.HUDEditorScreen
import dev.dyzjct.kura.module.ModuleManager
import melon.events.ConnectionEvent
import melon.events.PacketEvents
import melon.events.TickEvent
import melon.events.input.MouseScrollEvent
import melon.events.screen.ResolutionUpdateEvent
import melon.system.event.ListenerOwner
import melon.system.event.listener
import melon.system.event.safeEventListener
import net.minecraft.client.MinecraftClient
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket

internal object EventListenerManager : ListenerOwner() {
    private var displayWidth = 0
    private var displayHeight = 0

    fun call() {
        listener<TickEvent.Post>(true) {
            val mc = MinecraftClient.getInstance()
            if (mc.window.width != displayWidth || mc.window.height != displayHeight) {
                displayWidth = mc.window.width
                displayHeight = mc.window.height
                ResolutionUpdateEvent(mc.window.width, mc.window.height).post()
                GlStateManager._glUseProgram(0)
            }
        }

        safeEventListener<PacketEvents.Receive>(true) { event ->
            when (event.packet) {
                is DisconnectS2CPacket -> {
                    ModuleManager.onLogout()
                    ConnectionEvent.Disconnect.post()
                    ModuleManager.getToggleList().forEach { module ->
                        module.safeDisable()
                    }
                }
            }
        }

        safeEventListener<MouseScrollEvent>(true) { event ->
            when (mc.currentScreen) {
                is ClickGuiScreen -> {
                    GUIRender.mouseDrag(event.amount)
                }

                is HUDEditorScreen -> {
                    HUDRender.mouseDrag(event.amount)
                }
            }
        }
    }
}