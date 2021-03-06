package me.melijn.melijnbot.objects.utils

import me.melijn.melijnbot.objects.command.CommandContext
import me.melijn.melijnbot.objects.translation.PLACEHOLDER_ARG
import me.melijn.melijnbot.objects.translation.i18n

suspend fun getIntegerFromArgNMessage(context: CommandContext, index: Int, start: Int = Integer.MIN_VALUE, end: Int = Integer.MAX_VALUE): Int? {
    if (argSizeCheckFailed(context, index)) return null
    val arg = context.args[index]


    val int = arg.toIntOrNull()
    val language = context.getLanguage()
    when {
        int == null -> {
            val msg = i18n.getTranslation(language, "message.unknown.integer")
                .replace(PLACEHOLDER_ARG, arg)
            sendMsg(context, msg)
        }
        int < start -> {
            val msg = i18n.getTranslation(language, "message.tosmall.integer")
                .replace(PLACEHOLDER_ARG, arg)
                .replace("%min%", start.toString())
            sendMsg(context, msg)
            return null
        }
        int > end -> {
            val msg = i18n.getTranslation(language, "message.tobig.integer")
                .replace(PLACEHOLDER_ARG, arg)
                .replace("%max%", end.toString())
            sendMsg(context, msg)
            return null
        }
    }

    return int
}

suspend fun getFloatFromArgNMessage(context: CommandContext, index: Int, start: Float = Float.MIN_VALUE, end: Float = Float.MAX_VALUE): Float? {
    if (argSizeCheckFailed(context, index)) return null
    val arg = context.args[index]

    val float = arg.toFloatOrNull()
    val language = context.getLanguage()
    when {
        float == null -> {
            val msg = i18n.getTranslation(language, "message.unknown.float")
                .replace(PLACEHOLDER_ARG, arg)
            sendMsg(context, msg)
        }
        float < start -> {
            val msg = i18n.getTranslation(language, "message.tosmall.float")
                .replace(PLACEHOLDER_ARG, arg)
                .replace("%min%", start.toString())
            sendMsg(context, msg)
        }
        float > end -> {
            val msg = i18n.getTranslation(language, "message.tobig.float")
                .replace(PLACEHOLDER_ARG, arg)
                .replace("%max%", end.toString())
            sendMsg(context, msg)
        }
    }

    return float
}

suspend fun getBooleanFromArgN(context: CommandContext, index: Int): Boolean? {
    if (argSizeCheckFailed(context, index, true)) return null
    val arg = context.args[index]

    return when (arg.toLowerCase()) {
        "true", "yes", "on", "enable", "enabled", "positive", "+" -> true
        "false", "no", "off", "disable", "disabled", "negative", "-" -> false
        else -> null
    }
}

suspend fun getBooleanFromArgNMessage(context: CommandContext, index: Int): Boolean? {
    if (argSizeCheckFailed(context, index)) return null
    val arg = context.args[index]

    val bool = getBooleanFromArgN(context, index)
    if (bool == null) {
        val language = context.getLanguage()
        val msg = i18n.getTranslation(language, "message.unknown.boolean")
            .replace(PLACEHOLDER_ARG, arg)
        sendMsg(context, msg)
    }

    return bool
}

suspend fun argSizeCheckFailed(context: CommandContext, index: Int, silent: Boolean = false): Boolean {
    return if (context.args.size <= index) {
        if (!silent) sendSyntax(context, context.commandOrder.last().syntax)
        true
    } else {
        false
    }
}
