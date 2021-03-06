package me.melijn.melijnbot.commands.music

import me.melijn.melijnbot.objects.command.AbstractCommand
import me.melijn.melijnbot.objects.command.CommandCategory
import me.melijn.melijnbot.objects.command.CommandContext
import me.melijn.melijnbot.objects.command.hasPermission
import me.melijn.melijnbot.objects.utils.RunConditionUtil
import me.melijn.melijnbot.objects.utils.getVoiceChannelByArgNMessage
import me.melijn.melijnbot.objects.utils.notEnoughPermissionsAndMessage
import me.melijn.melijnbot.objects.utils.sendMsg
import net.dv8tion.jda.api.Permission

class SummonCommand : AbstractCommand("command.summon") {

    init {
        id = 94
        name = "summon"
        aliases = arrayOf("joinChannel")
        commandCategory = CommandCategory.MUSIC
    }

    override suspend fun execute(context: CommandContext) {
        if (context.args.isEmpty()) {
            if (!RunConditionUtil.checkSameVCBotAloneOrUserDJ(context.container, context.event, this, context.getLanguage())) return
            val vc = context.member.voiceState?.channel ?: throw IllegalStateException("I messed up")
            if (notEnoughPermissionsAndMessage(context, vc, Permission.VOICE_SPEAK, Permission.VOICE_CONNECT)) return

            val premium = context.daoManager.musicNodeWrapper.isPremium(context.guildId)
            context.lavaManager.openConnection(vc, premium)
            val msg = context.getTranslation("$root.summoned")
            sendMsg(context, msg)
        } else {
            val vc = getVoiceChannelByArgNMessage(context, 0) ?: return
            if (!hasPermission(context, "summon.other", true)) {
                sendMissingPermissionMessage(context, "summon.other")
                return
            }
            if (notEnoughPermissionsAndMessage(context, vc, Permission.VOICE_SPEAK, Permission.VOICE_CONNECT)) return
            if (!RunConditionUtil.checkBotAloneOrUserDJ(context.container, context.event, this, context.getLanguage())) return

            val premium = context.daoManager.musicNodeWrapper.isPremium(context.guildId)
            context.lavaManager.openConnection(vc, premium)
            val msg = context.getTranslation("$root.summoned")
            sendMsg(context, msg)
        }
    }
}