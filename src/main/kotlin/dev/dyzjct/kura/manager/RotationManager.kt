package dev.dyzjct.kura.manager

import dev.dyzjct.kura.utils.math.RotationUtils.getRotationTo
import base.events.PacketEvents
import base.events.player.PlayerMotionEvent
import base.system.event.AlwaysListening
import base.system.event.safeEventListener
import base.utils.concurrent.threads.runSafe
import net.minecraft.block.Blocks
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import base.utils.math.toVec3dCenter
import base.utils.math.vector.Vec2f
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object RotationManager : AlwaysListening {
    private val rotationMap = ConcurrentHashMap<Vec2f, Boolean>()
    private var spoofMap = CopyOnWriteArrayList<Vec2f>()
    private var stop = false

    fun onInit() {
        safeEventListener<PlayerMotionEvent>(Int.MAX_VALUE) { event ->
            if (stop) return@safeEventListener
            rotationMap.toSortedMap(Comparator.comparing { (_, prio) -> prio }).forEach { (vec, _) ->
                val packet = Vec2f(vec.x, vec.y)
                event.setRotation(packet.x, packet.y)
                if (world.getBlockState(player.blockPos).block == Blocks.OBSIDIAN || world.getBlockState(player.blockPos).block == Blocks.CRYING_OBSIDIAN) {
                    spoofMap.add(packet)
                    return@forEach
                }
                rotationMap.remove(packet)
            }
        }

        safeEventListener<PacketEvents.Send>(Int.MAX_VALUE) { event ->
            if (stop) return@safeEventListener
            spoofMap.forEach {
                when (event.packet) {
                    is PlayerMoveC2SPacket -> {
                        val packet = it
                        event.packet.yaw = packet.x
                        event.packet.pitch = packet.y
                        spoofMap.remove(packet)
                        rotationMap.remove(packet)
                    }
                }
            }
        }
    }

    fun stopRotation() {
        stop = true
    }

    fun startRotation() {
        stop = false
    }

    @JvmStatic
    fun addRotations(yaw: Float, pitch: Float, prio: Boolean = false) {
        rotationMap[Vec2f(yaw, pitch)] = prio
    }

    @JvmStatic
    fun addRotations(rotation: Vec2f, prio: Boolean = false) {
        rotationMap[rotation] = prio
    }

    @JvmStatic
    fun addRotations(blockPos: BlockPos, prio: Boolean = false, side: Boolean = false) {
        runSafe {
            rotationMap[getRotationTo(blockPos.toVec3dCenter(), side)] = prio
        }
    }

    @JvmStatic
    fun addRotations(vec3d: Vec3d, prio: Boolean = false, side: Boolean = false) {
        runSafe {
            rotationMap[getRotationTo(vec3d, side)] = prio
        }
    }
}