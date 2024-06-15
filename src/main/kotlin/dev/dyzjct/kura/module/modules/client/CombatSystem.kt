package dev.dyzjct.kura.module.modules.client

import base.system.event.SafeClientEvent
import base.utils.chat.ChatUtil
import dev.dyzjct.kura.gui.clickgui.ClickGuiScreen
import dev.dyzjct.kura.manager.AuraPrioManager
import dev.dyzjct.kura.manager.FileManager
import dev.dyzjct.kura.module.Category
import dev.dyzjct.kura.module.Module
import dev.dyzjct.kura.setting.BooleanSetting
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket
import net.minecraft.util.Hand

object CombatSystem : Module(
    name = "CombatSystem",
    langName = "战斗系统",
    category = Category.CLIENT,
    type = Type.Both
) {
    val combatMode = msetting("CombatMode", CombatMode.Strong)
    val spoofMode = msetting("SpoofMode", SpoofMode.Normal).enumIs(combatMode, CombatMode.Strong)
    val autoSwitch by bsetting("AutoSwitch", false).enumIs(combatMode, CombatMode.Strong)
    val eating by bsetting("EatingPause", true).enumIs(combatMode, CombatMode.Strong)
    val smartAura by bsetting("SmartAura", false).enumIs(combatMode, CombatMode.Strong)
    val autoToggle by bsetting("[CA/AA]AutoToggle", false).enumIs(combatMode, CombatMode.Strong).isFalse { smartAura }
    val mainToggle by msetting("MainToggle", MainToggle.Crystal).enumIs(combatMode, CombatMode.Strong)
        .isFalse { smartAura }
    var predictTicks by isetting("PredictedTicks", 4, 0, 20)
    var maxTargets by isetting("MaxTarget", 3, 1, 8)
    val targetRange by dsetting("TargetRange", 8.0, 0.0, 12.0)
    val placeRange by dsetting("PlaceRange", 6.0, 0.0, 8.0)
    val attackRange by dsetting("AttackRange", 6.0, 0.0, 8.0)
    val interactRange by dsetting("InteractRange", 6.0, 0.0, 8.0)
    val kaRange by dsetting("KARange", 6.0, 0.0, 8.0)
    private val swing by bsetting("Swing", true)
    private val packetSwing by bsetting("PacketSwing", true).isTrue { swing }
    private val swingHand by msetting("SwingHand", SwingHand.MainHand).isTrue { swing }
    val debug by bsetting("Debug", false)

    var bestAura: BestAura? = null

    init {
        combatMode.onChange<BooleanSetting> { value: Enum<*> ->
            turn(value)
        }
    }

    private fun turn(value: Enum<*>) {
        if (value == CombatMode.Strong) {
            if (debug) ChatUtil.sendMessage("Turn To Strong")
            FileManager.saveAll(CombatMode.Ghost.name)
            FileManager.loadCombatSystem()
            FileManager.loadAll(CombatMode.Strong.name)
        } else {
            if (debug) ChatUtil.sendMessage("Turn To Ghost")
            FileManager.saveAll(CombatMode.Strong.name)
            FileManager.loadCombatSystem()
            FileManager.loadAll(CombatMode.Ghost.name)
        }
        ClickGuiScreen.updatePanelModuleOnModeChange()
    }

    fun SafeClientEvent.swing() {
        if (swing) {
            if (packetSwing) connection.sendPacket(
                HandSwingC2SPacket(
                    when (swingHand) {
                        SwingHand.OffHand -> Hand.OFF_HAND
                        else -> Hand.MAIN_HAND
                    }
                )
            ) else player.swingHand(
                when (swingHand) {
                    SwingHand.OffHand -> Hand.OFF_HAND
                    else -> Hand.MAIN_HAND
                }
            )
            player.resetLastAttackedTicks()
        }
    }

    enum class CombatMode {
        Strong, Ghost
    }

    enum class SpoofMode {
        Normal, Swap, China
    }

    enum class MainToggle {
        Crystal, Anchor
    }

    enum class SwingHand {
        MainHand, OffHand
    }

    enum class BestAura {
        Crystal, Anchor, Sword
    }

    class Auras(val aura: BestAura, val damage: Double)
}