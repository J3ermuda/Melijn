package me.melijn.melijnbot.commands.administration


import kotlinx.coroutines.future.await
import me.melijn.melijnbot.enums.Language
import me.melijn.melijnbot.objects.command.AbstractCommand
import me.melijn.melijnbot.objects.command.CommandCategory
import me.melijn.melijnbot.objects.command.CommandContext
import me.melijn.melijnbot.objects.translation.MESSAGE_UNKNOWN_LANGUAGE
import me.melijn.melijnbot.objects.utils.getEnumFromArgNMessage
import me.melijn.melijnbot.objects.utils.sendMsg
import me.melijn.melijnbot.objects.utils.sendMsgCodeBlock

class SetLanguageCommand : AbstractCommand("command.setlanguage") {

    init {
        id = 2
        name = "setLanguage"
        aliases = arrayOf("setLang", "sl")
        children = arrayOf(
            ListArg(root)
        )
        commandCategory = CommandCategory.ADMINISTRATION
    }


    override suspend fun execute(context: CommandContext) {
        if (context.args.isEmpty()) {
            sendCurrentLang(context)
        } else {
            setLang(context)
        }
    }

    private suspend fun sendCurrentLang(context: CommandContext) {
        val wrapper = context.daoManager.guildLanguageWrapper
        val lang = wrapper.languageCache.get(context.guildId).await()

        val msg = context.getTranslation(
            if (lang.isBlank()) {
                "$root.show.unset"
            } else {
                "$root.show.set"
            }
        ).replace("%language%", lang)

        sendMsg(context, msg)
    }

    private suspend fun setLang(context: CommandContext) {
        val lang: String
        val shouldUnset = "null".equals(context.rawArg, true)

        lang = if (shouldUnset) {
            ""
        } else {
            getEnumFromArgNMessage<Language>(context, 0, MESSAGE_UNKNOWN_LANGUAGE)?.toString() ?: return
        }

        val wrapper = context.daoManager.guildLanguageWrapper
        wrapper.setLanguage(context.guildId, lang)

        val possible = if (shouldUnset) {
            "un"
        } else {
            ""
        }

        val msg = context.getTranslation("$root.${possible}set")
            .replace("%language%", lang)
        sendMsg(context, msg)
    }

    class ListArg(parent: String) : AbstractCommand("$parent.list") {

        init {
            name = "list"
        }

        override suspend fun execute(context: CommandContext) {
            var msg = context.getTranslation("$root.title")
            msg += Language.values()
                .withIndex()
                .joinToString("\n", "```INI\n", "```") { (index, lang) ->
                    "$index - [$lang]"
                }
            sendMsgCodeBlock(context, msg, "INI")
        }
    }
}