package me.melijn.melijnbot.commands.image

import me.melijn.melijnbot.commandutil.image.ImageCommandUtil
import me.melijn.melijnbot.commandutil.image.ImageCommandUtil.defaultOffsetArgParser
import me.melijn.melijnbot.objects.command.AbstractCommand
import me.melijn.melijnbot.objects.command.CommandCategory
import me.melijn.melijnbot.objects.command.CommandContext
import me.melijn.melijnbot.objects.command.RunCondition
import me.melijn.melijnbot.objects.utils.ImageUtils
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.utils.data.DataObject
import java.awt.image.BufferedImage
import java.lang.Integer.max

class BlurCommand : AbstractCommand("command.blur") {

    init {
        id = 135
        name = "blur"
        aliases = arrayOf("blurGif")
        discordChannelPermissions = arrayOf(Permission.MESSAGE_ATTACH_FILES)
        runConditions = arrayOf(RunCondition.VOTED)
        commandCategory = CommandCategory.IMAGE
    }

    override suspend fun execute(context: CommandContext) {
        if (context.commandParts[1].equals("blurGif", true)) {
            executeGif(context)
        } else {
            executeNormal(context)
        }
    }

    private suspend fun executeNormal(context: CommandContext) {
        ImageCommandUtil.executeNormalEffect(context, effect = { image, argData ->
            ImageUtils.blur(image, argData.getInt("offset"))

        }, argDataParser = { argInt: Int, argData: DataObject, imgData: DataObject ->
            defaultOffsetArgParser(context, argInt, argData, imgData)

        }, imgDataParser = { img: BufferedImage, imgData: DataObject ->
            imgData.put("lower", 1)
            imgData.put("higher", max(img.height, img.width))
            imgData.put("defaultOffset", max(max(img.width, img.height) / 75, 1))

        })
    }

    private suspend fun executeGif(context: CommandContext) {
        ImageCommandUtil.executeGifEffect(context, effect = { image, argData ->
            ImageUtils.blur(image, argData.getInt("offset"), true)

        }, argDataParser = { argInt: Int, argData: DataObject, imgData: DataObject ->
            defaultOffsetArgParser(context, argInt, argData, imgData)

        }, imgDataParser = { img: BufferedImage, imgData: DataObject ->
            imgData.put("lower", 1)
            imgData.put("higher", max(img.height, img.width))
            imgData.put("defaultOffset", max(max(img.width, img.height) / 75, 1))

        }, argumentAmount = 1, debug = false)
    }
}