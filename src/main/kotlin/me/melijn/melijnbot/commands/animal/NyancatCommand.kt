package me.melijn.melijnbot.commands.animal

import me.melijn.melijnbot.objects.command.AbstractCommand
import me.melijn.melijnbot.objects.command.CommandCategory
import me.melijn.melijnbot.objects.command.CommandContext
import me.melijn.melijnbot.objects.command.RunCondition
import me.melijn.melijnbot.objects.embed.Embedder
import me.melijn.melijnbot.objects.utils.sendEmbed
import kotlin.random.Random


class NyancatCommand : AbstractCommand("command.nyancat") {

    init {
        id = 49
        name = "nyancat"
        aliases = arrayOf("nyan", "nya")
        runConditions = arrayOf(RunCondition.VOTED)
        commandCategory = CommandCategory.ANIMAL
    }

    override suspend fun execute(context: CommandContext) {
        val eb = Embedder(context)
        val title = context.getTranslation("$root.title")

        eb.setTitle(title)
        eb.setImage(getRandomNyancatUrl())
        sendEmbed(context, eb.build())
    }

    private fun getRandomNyancatUrl(): String {
        val randomInt = Random.nextInt(2, 33)
        return "https://github.com/ToxicMushroom/nyan-cats/raw/master/cat%20($randomInt).gif"
    }
}