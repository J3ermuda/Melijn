package me.melijn.melijnbot.commands.administration

import me.melijn.melijnbot.enums.LogChannelType
import me.melijn.melijnbot.objects.command.AbstractCommand
import me.melijn.melijnbot.objects.command.CommandCategory
import me.melijn.melijnbot.objects.command.CommandContext
import me.melijn.melijnbot.objects.translation.PLACEHOLDER_CHANNEL
import me.melijn.melijnbot.objects.utils.asTag
import me.melijn.melijnbot.objects.utils.checks.getAndVerifyLogChannelByType
import me.melijn.melijnbot.objects.utils.getTextChannelByArgsNMessage
import me.melijn.melijnbot.objects.utils.sendMsg
import me.melijn.melijnbot.objects.utils.sendSyntax

class SetLogChannelCommand : AbstractCommand("command.setlogchannel") {

    init {
        id = 21
        name = "setLogChannel"
        aliases = arrayOf("slc")
        commandCategory = CommandCategory.ADMINISTRATION
    }

    override suspend fun execute(context: CommandContext) {
        if (context.args.isEmpty()) {
            sendSyntax(context)
            return
        }

        val matchingEnums: List<LogChannelType> = LogChannelType.getMatchingTypesFromNode(context.args[0])
        if (matchingEnums.isEmpty()) {
            sendSyntax(context)
            return
        }
        if (matchingEnums.size > 1)
            handleEnums(context, matchingEnums)
        else handleEnum(context, matchingEnums[0])

    }

    private suspend fun handleEnum(context: CommandContext, logChannelType: LogChannelType) {
        if (context.args.size > 1) {
            setChannel(context, logChannelType)
        } else {
            displayChannel(context, logChannelType)
        }
    }

    private suspend fun displayChannel(context: CommandContext, logChannelType: LogChannelType) {
        val daoManager = context.daoManager
        val channel = context.guild.getAndVerifyLogChannelByType(daoManager, logChannelType)

        val msg = (if (channel != null) {
            context.getTranslation("$root.show.set.single")
                .replace(PLACEHOLDER_CHANNEL, channel.asTag)
        } else {
            context.getTranslation("$root.show.unset.single")
        }).replace("%logChannelType%", logChannelType.text)

        sendMsg(context, msg)
    }


    private suspend fun setChannel(context: CommandContext, logChannelType: LogChannelType) {
        if (context.args.size < 2) {
            sendSyntax(context)
            return
        }

        val daoWrapper = context.daoManager.logChannelWrapper
        val msg = if (context.args[1].equals("null", true)) {

            daoWrapper.removeChannel(context.guildId, logChannelType)
            context.getTranslation("$root.unset.single")
                .replace("%logChannelType%", logChannelType.text)
        } else {
            val channel = getTextChannelByArgsNMessage(context, 1) ?: return
            daoWrapper.setChannel(context.guildId, logChannelType, channel.idLong)

            context.getTranslation("$root.set.single")
                .replace("%logChannelType%", logChannelType.text)
                .replace(PLACEHOLDER_CHANNEL, channel.asTag)

        }
        sendMsg(context, msg)
    }

    private suspend fun handleEnums(context: CommandContext, logChannelTypes: List<LogChannelType>) {
        if (context.args.size > 1) {
            setChannels(context, logChannelTypes)
        } else {
            displayChannels(context, logChannelTypes)
        }
    }

    private suspend fun displayChannels(context: CommandContext, logChannelTypes: List<LogChannelType>) {
        val daoManager = context.daoManager
        val title = context.getTranslation("$root.show.multiple")
            .replace("%channelCount%", logChannelTypes.size.toString())
            .replace("%logChannelTypeNode%", context.args[0])

        val lines = emptyList<String>().toMutableList()

        for (type in logChannelTypes) {
            val channel = context.guild.getAndVerifyLogChannelByType(daoManager, type)
            lines += "${type.text}: " + (channel?.asMention ?: "/")
        }

        val content = lines.joinToString(separator = "\n", prefix = "\n")
        val msg = title + content
        sendMsg(context, msg)
    }

    private suspend fun setChannels(context: CommandContext, logChannelTypes: List<LogChannelType>) {
        if (context.args.size < 2) {
            sendSyntax(context)
            return
        }

        val daoWrapper = context.daoManager.logChannelWrapper
        val msg = if (context.args[1].equals("null", true)) {

            daoWrapper.removeChannels(context.guildId, logChannelTypes)

            context.getTranslation("$root.unset.multiple")
                .replace("%channelCount%", logChannelTypes.size.toString())
                .replace("%logChannelTypeNode%", context.args[0])
        } else {
            val channel = getTextChannelByArgsNMessage(context, 1) ?: return
            daoWrapper.setChannels(context.guildId, logChannelTypes, channel.idLong)


            context.getTranslation("$root.set.multiple")
                .replace("%channelCount%", logChannelTypes.size.toString())
                .replace("%logChannelTypeNode%", context.args[0])
                .replace(PLACEHOLDER_CHANNEL, channel.asTag)

        }
        sendMsg(context, msg)
    }
}