package me.melijn.melijnbot.commands.animal

import me.melijn.melijnbot.objects.command.AbstractCommand
import me.melijn.melijnbot.objects.command.CommandCategory
import me.melijn.melijnbot.objects.command.CommandContext
import me.melijn.melijnbot.objects.embed.Embedder
import me.melijn.melijnbot.objects.translation.MISSING_IMAGE_URL
import me.melijn.melijnbot.objects.utils.sendEmbed
import me.melijn.melijnbot.objects.web.WebManager
import me.melijn.melijnbot.objects.web.WebUtils

class PandaCommand : AbstractCommand("command.panda") {

    init {
        id = 50
        name = "panda"
        commandCategory = CommandCategory.ANIMAL
    }

    override suspend fun execute(context: CommandContext) {
        val eb = Embedder(context)
        val title = context.getTranslation("$root.title")

        val web = context.webManager
        eb.setTitle(title)
        eb.setImage(getRandomPandaUrl(web))
        sendEmbed(context, eb.build())
    }

    private suspend fun getRandomPandaUrl(webManager: WebManager): String {
        val reply = WebUtils.getJsonFromUrl(webManager.httpClient, "https://some-random-api.ml/img/panda")
            ?: return MISSING_IMAGE_URL
        return reply.getString("link")
    }
}