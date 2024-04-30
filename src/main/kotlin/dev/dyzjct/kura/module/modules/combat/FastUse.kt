package dev.dyzjct.kura.module.modules.combat

import dev.dyzjct.kura.module.Category
import dev.dyzjct.kura.module.Module
import base.utils.concurrent.threads.runSafe
import net.minecraft.item.BlockItem
import net.minecraft.item.BowItem
import net.minecraft.item.Items

object FastUse :
    Module(category = Category.COMBAT, langName = "快速使用物品", description = "Use items faster", name = "FastUse") {
    private val delay = isetting("Delay", 1, 0, 10)
    private val exp = bsetting("XP", true)
    // 弓箭没必要
    // private val bow = bsetting("Bow", false)
    private val block = bsetting("Block", false)

    init {
        onMotion {
            if (player.mainHandStack.item == Items.EXPERIENCE_BOTTLE && exp.value ||
                player.mainHandStack.item is BlockItem && block.value) {
                mc.itemUseCooldown =
                    mc.itemUseCooldown.coerceAtMost(delay.value)
            }
        }
    }

    override fun onDisable() {
        runSafe {
            mc.itemUseCooldown = 2
        }
    }
}
