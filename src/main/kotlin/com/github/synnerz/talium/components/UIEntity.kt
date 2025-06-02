package com.github.synnerz.talium.components

import com.github.synnerz.talium.utils.Renderer
import com.mojang.authlib.GameProfile
import com.mojang.authlib.minecraft.MinecraftProfileTexture
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.resources.DefaultPlayerSkin
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EnumPlayerModelParts
import net.minecraft.scoreboard.Team
import net.minecraft.util.ChatComponentText
import net.minecraft.util.IChatComponent
import net.minecraft.util.ResourceLocation
import java.awt.Color
import java.util.UUID
import kotlin.math.atan

open class UIEntity @JvmOverloads constructor(
    _x: Double,
    _y: Double,
    _width: Double,
    _height: Double,
    var entityIn: EntityLivingBase,
    var scale: Float = 1f,
    parent: UIBase? = null
) : UIBase(_x, _y, _width, _height, parent) {
    override var bgColor: Color = Color(255, 255, 255, 255)

    open fun getmx(): Float = x.toFloat() - Renderer.getMouseX(scaledResolution!!)

    open fun getmy(): Float = -y.toFloat() + Renderer.getMouseY(scaledResolution!!)

    override fun render() {
        entityIn.onUpdate()
        GlStateManager.pushMatrix()
        GlStateManager.enableTexture2D()
        GlStateManager.enableColorMaterial()
        GlStateManager.translate(x, y, 50.0)
        GlStateManager.scale(-scale, scale, scale)
        GlStateManager.rotate(180f, 0f, 0f, 1f)

        val mx = getmx()
        val my = getmy()
        val renderYawOffset = entityIn.renderYawOffset
        val entityYaw = entityIn.rotationYaw
        val entityRotation = entityIn.rotationPitch
        val entityPrevRotationYawHead = entityIn.prevRotationYawHead
        val entityRotationYawHead = entityIn.rotationYawHead

        GlStateManager.rotate(135f, 0f, 1f, 0f)
        RenderHelper.enableStandardItemLighting()
        GlStateManager.rotate(-135f, 0f, 1f, 0f)
        GlStateManager.rotate(25f, 1f, 0f, 0f)

        entityIn.renderYawOffset = atan(mx / 40f) * 20f
        entityIn.rotationYaw = atan(mx / 40f) * 40f
        entityIn.rotationPitch = atan(my / 40f) * 20f
        entityIn.rotationYawHead = entityIn.rotationYaw
        entityIn.prevRotationYawHead = entityIn.rotationYaw

        val renderManager = Minecraft.getMinecraft().renderManager

        renderManager.setPlayerViewY(180f)
        renderManager.isRenderShadow = false
        renderManager.renderEntityWithPosYaw(entityIn, 0.0, 0.0, 0.0, 0f, 1f)

        entityIn.renderYawOffset = renderYawOffset
        entityIn.rotationYaw = entityYaw
        entityIn.rotationPitch = entityRotation
        entityIn.rotationYawHead = entityRotationYawHead
        entityIn.prevRotationYawHead = entityPrevRotationYawHead

        RenderHelper.disableStandardItemLighting()
        GlStateManager.disableTexture2D()
        GlStateManager.popMatrix()
    }

    companion object {
        @JvmOverloads
        @JvmStatic
        fun fromAny(
            _x: Double,
            _y: Double,
            _width: Double,
            _height: Double,
            entityIn: Any,
            scale: Float = 1f,
            parent: UIBase? = null
        ) : UIEntity {
            // Unchecked cast wooo
            return UIEntity(_x, _y, _width, _height, entityIn as EntityLivingBase, scale, parent)
        }

        @JvmOverloads
        @JvmStatic
        fun fromPlayer(
            _x: Double,
            _y: Double,
            _width: Double,
            _height: Double,
            uuid: String,
            name: String,
            scale: Float = 1f,
            parent: UIBase? = null
        ) : UIEntity {
            val uuidIn = UUID.fromString(uuid)
            val fakeProfile = Minecraft.getMinecraft().sessionService.fillProfileProperties(GameProfile(uuidIn, name), true)
            var skinLocation: ResourceLocation? = null
            var capeLocation: ResourceLocation? = null
            var skinType: String? = null

            val entityIn = object : EntityOtherPlayerMP(Minecraft.getMinecraft().theWorld, fakeProfile) {
                override fun getLocationSkin(): ResourceLocation = skinLocation ?: DefaultPlayerSkin.getDefaultSkin(uniqueID)
                override fun getSkinType(): String = skinType ?: "default"
                override fun getLocationCape(): ResourceLocation = capeLocation ?: super.getLocationCape()
                override fun getAlwaysRenderNameTag(): Boolean = false
                override fun hasCustomName(): Boolean = false
                override fun getDisplayName(): IChatComponent = ChatComponentText("")
                override fun getTeam(): Team? = null
                override fun moveEntityWithHeading(strafe: Float, forward: Float) {}
                override fun isOnTeam(teamIn: Team?): Boolean = false
                override fun canEntityBeSeen(entityIn: Entity?): Boolean = false
            }

            Minecraft.getMinecraft().skinManager.loadProfileTextures(entityIn.gameProfile, { type, loc1, ptexture ->
                when (type) {
                    MinecraftProfileTexture.Type.SKIN -> {
                        skinLocation = loc1
                        skinType = ptexture.getMetadata("model") ?: "default"
                    }
                    MinecraftProfileTexture.Type.CAPE -> {
                        capeLocation = loc1
                    }
                    else -> return@loadProfileTextures
                }
            }, false)

            var bytes = 0
            for (layer in EnumPlayerModelParts.entries) {
                bytes = bytes or layer.partMask
            }

            entityIn.dataWatcher.updateObject(10, bytes.toByte())

            return UIEntity(_x, _y, _width, _height, entityIn, scale, parent)
        }
    }
}