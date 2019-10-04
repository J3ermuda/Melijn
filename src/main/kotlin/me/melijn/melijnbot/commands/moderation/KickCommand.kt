package me.melijn.melijnbot.commands.moderation

import kotlinx.coroutines.future.await
import me.melijn.melijnbot.database.kick.Kick
import me.melijn.melijnbot.enums.LogChannelType
import me.melijn.melijnbot.objects.command.AbstractCommand
import me.melijn.melijnbot.objects.command.CommandCategory
import me.melijn.melijnbot.objects.command.CommandContext
import me.melijn.melijnbot.objects.translation.PLACEHOLDER_USER
import me.melijn.melijnbot.objects.translation.i18n
import me.melijn.melijnbot.objects.utils.*
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.*
import java.awt.Color

class KickCommand : AbstractCommand("command.kick") {

    init {
        id = 30
        name = "kick"
        commandCategory = CommandCategory.MODERATION
    }

    override suspend fun execute(context: CommandContext) {
        if (context.args.isEmpty()) {
            sendSyntax(context, syntax)
            return
        }
        val targetMember = getMemberByArgsNMessage(context, 0) ?: return
        if (!context.getGuild().selfMember.canInteract(targetMember)) {
            val language = context.getLanguage()
            val msg = i18n.getTranslation(language, "$root.cannotkick")
                .replace(PLACEHOLDER_USER, targetMember.asTag)
            sendMsg(context, msg)
            return
        }

        var reason = context.rawArg
            .replaceFirst(context.args[0], "")
            .trim()
        if (reason.isBlank()) reason = "/"
        reason = reason.trim()

        val kick = Kick(
            context.getGuildId(),
            targetMember.idLong,
            context.authorId,
            reason
        )


        val language = context.getLanguage()
        val kicking = i18n.getTranslation(language, "message.kicking")
        try {
            val privateChannel = targetMember.user.openPrivateChannel().await()
            val message = privateChannel.sendMessage(kicking).await()

            continueKicking(context, targetMember, kick, message)
        } catch (t: Throwable) {
            continueKicking(context, targetMember, kick)
        }
    }

    private suspend fun continueKicking(context: CommandContext, targetMember: Member, kick: Kick, kickingMessage: Message? = null) {
        val guild = context.getGuild()
        val author = context.getAuthor()
        val language = context.getLanguage()

        val kickedMessageDm = getKickMessage(language, guild, targetMember.user, author, kick)
        val warnedMessageLc = getKickMessage(language, guild, targetMember.user, author, kick, true, targetMember.user.isBot, kickingMessage != null)

        context.daoManager.kickWrapper.addKick(kick)
        val msg = try {
            context.getGuild().kick(targetMember, kick.reason).await()
            kickingMessage?.editMessage(
                kickedMessageDm
            )?.override(true)?.queue()

            val logChannelWrapper = context.daoManager.logChannelWrapper
            val logChannelId = logChannelWrapper.logChannelCache.get(Pair(guild.idLong, LogChannelType.KICK)).await()
            val logChannel = guild.getTextChannelById(logChannelId)
            logChannel?.let { it1 -> sendEmbed(context.daoManager.embedDisabledWrapper, it1, warnedMessageLc) }

            i18n.getTranslation(language, "$root.success")
                .replace(PLACEHOLDER_USER, targetMember.asTag)
                .replace("%reason%", kick.reason)

        } catch (t: Throwable) {
            kickingMessage?.editMessage("failed to kick")?.queue()

            i18n.getTranslation(language, "$root.failure")
                .replace(PLACEHOLDER_USER, targetMember.asTag)
                .replace("%cause%", t.message ?: "unknown (contact support for info)")

        }
        sendMsg(context, msg)
    }
}

fun getKickMessage(
    language: String,
    guild: Guild,
    kickedUser: User,
    kickAuthor: User,
    kick: Kick,
    lc: Boolean = false,
    isBot: Boolean = false,
    received: Boolean = true
): MessageEmbed {
    val eb = EmbedBuilder()

    var description = "```LDIF"
    if (!lc) {
        description += i18n.getTranslation(language, "message.punishment.nlc")
            .replace("%guildName%", guild.name)
            .replace("%guildId%", guild.name)
    }

    description += i18n.getTranslation(language, "message.punishment.kick.description")
        .replace("%kickAuthor%", kickAuthor.asTag)
        .replace("%kickAuthorId%", kickAuthor.id)
        .replace("%kicked%", kickedUser.asTag)
        .replace("%kickedId%", kickedUser.id)
        .replace("%reason%", kick.reason)
        .replace("%moment%", (kick.moment.asEpochMillisToDateTime()))

    val extraDesc: String = if (!received || isBot) {
        i18n.getTranslation(language,
            if (isBot) {
                "message.punishment.extra.bot"
            } else {
                "message.punishment.extra.dm"
            }
        )
    } else {
        ""
    }
    description += extraDesc
    description += "```"

    val author = i18n.getTranslation(language, "message.punishment.kick.author")
        .replace(PLACEHOLDER_USER, kickAuthor.asTag)
        .replace("%spaces%", " ".repeat(45).substring(0, 45 - kickAuthor.name.length) + "\u200B")

    eb.setAuthor(author, null, kickAuthor.effectiveAvatarUrl)
    eb.setDescription(description)
    eb.setThumbnail(kickedUser.effectiveAvatarUrl)
    eb.setColor(Color.ORANGE)
    return eb.build()
}