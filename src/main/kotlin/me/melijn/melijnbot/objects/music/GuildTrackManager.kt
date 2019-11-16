package me.melijn.melijnbot.objects.music

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException
import com.sedmelluq.discord.lavaplayer.track.AudioTrack
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import lavalink.client.player.IPlayer
import lavalink.client.player.event.AudioEventAdapterWrapped
import me.melijn.melijnbot.Container
import me.melijn.melijnbot.MelijnBot
import me.melijn.melijnbot.database.DaoManager
import me.melijn.melijnbot.enums.LogChannelType
import me.melijn.melijnbot.objects.utils.LogUtils
import me.melijn.melijnbot.objects.utils.checks.getAndVerifyLogChannelByType
import me.melijn.melijnbot.objects.utils.sendEmbed
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.MessageEmbed
import org.slf4j.LoggerFactory
import java.util.*


class GuildTrackManager(
    val guildId: Long,
    val daoManager: DaoManager,
    val lavaManager: LavaManager,
    val iPlayer: IPlayer
) : AudioEventAdapterWrapped() {

    val playingTrack: AudioTrack? = iPlayer.playingTrack
    val logger = LoggerFactory.getLogger(this::class.java.name + " - $guildId")

    val resumeMomentMessageMap = mutableMapOf<Long, MessageEmbed>()
    val pauseMomentMessageMap = mutableMapOf<Long, MessageEmbed>()
    val startMomentMessageMap = mutableMapOf<Long, MessageEmbed>()


    var loopedTrack = false
    var loopedQueue = false

    var tracks: Queue<AudioTrack> = LinkedList()
    fun trackSize() = tracks.size


    fun nextTrack(lastTrack: AudioTrack) {
        if (tracks.isEmpty()) {
            if (loopedQueue || loopedTrack) {
                iPlayer.playTrack(lastTrack.makeClone())
                return
            }
            lavaManager.closeConnection(guildId)
            return
        }

        if (loopedTrack) {
            iPlayer.playTrack(lastTrack.makeClone())
            return
        }

        val track: AudioTrack = tracks.poll()
        if (track == lastTrack) iPlayer.playTrack(track.makeClone()) else iPlayer.playTrack(track)
        if (loopedQueue) {
            tracks.add(lastTrack)
        }
    }

    fun queue(track: AudioTrack) {
        if (iPlayer.playingTrack == null) {
            iPlayer.playTrack(track)
        } else {
            tracks.offer(track)
        }
    }

    fun shuffle() {
        tracks = LinkedList(tracks.shuffled())
    }


    override fun onPlayerResume(player: AudioPlayer) {
        Container.instance.taskManager.async {
            val data = player.playingTrack.userData as TrackUserData
            val embed = getResumedEmbedFromMap(data.currentTime) ?: return@async
            val guild = getAndCheckGuild() ?: return@async

            val channel = guild.getAndVerifyLogChannelByType(LogChannelType.MUSIC, daoManager.logChannelWrapper)
                ?: return@async
            sendEmbed(daoManager.embedDisabledWrapper, channel, embed)
        }
    }

    override fun onPlayerPause(player: AudioPlayer) {
        Container.instance.taskManager.async {
            val data = player.playingTrack.userData as TrackUserData
            val embed = getPausedEmbedFromMap(data.currentTime) ?: return@async
            val guild = getAndCheckGuild() ?: return@async

            val channel = guild.getAndVerifyLogChannelByType(LogChannelType.MUSIC, daoManager.logChannelWrapper)
                ?: return@async
            sendEmbed(daoManager.embedDisabledWrapper, channel, embed)
        }
    }

    override fun onTrackStuck(player: AudioPlayer?, track: AudioTrack, thresholdMs: Long) {
        logger.debug("track stuck $thresholdMs")
    }

    override fun onTrackException(player: AudioPlayer?, track: AudioTrack, exception: FriendlyException) {
        val guild = getAndCheckGuild() ?: return
        runBlocking {
            LogUtils.sendMusicPlayerException(daoManager, guild, track, exception)
        }
    }

    override fun onTrackStart(player: AudioPlayer?, track: AudioTrack) {
        Container.instance.taskManager.async {
            val data = track.userData as TrackUserData
            val embed = getStartEmbedFromMap(data.currentTime) ?: return@async
            val guild = getAndCheckGuild() ?: return@async

            val channel = guild.getAndVerifyLogChannelByType(LogChannelType.MUSIC, daoManager.logChannelWrapper)
                ?: return@async
            sendEmbed(daoManager.embedDisabledWrapper, channel, embed)
        }
    }

    suspend fun getStartEmbedFromMap(time: Long, nesting: Int = 0, nestCountLimit: Int = 5): MessageEmbed? {
        return startMomentMessageMap.getOrElse(time) {
            delay(500)
            return if (nesting >= nestCountLimit) {
                null
            } else {
                getStartEmbedFromMap(time, nesting + 1, nestCountLimit)
            }
        }
    }

    suspend fun getPausedEmbedFromMap(time: Long, nesting: Int = 0, nestCountLimit: Int = 5): MessageEmbed? {
        return pauseMomentMessageMap.getOrElse(time) {
            delay(500)
            return if (nesting >= nestCountLimit) {
                null
            } else {
                getStartEmbedFromMap(time, nesting + 1, nestCountLimit)
            }
        }
    }

    suspend fun getResumedEmbedFromMap(time: Long, nesting: Int = 0, nestCountLimit: Int = 5): MessageEmbed? {
        return resumeMomentMessageMap.getOrElse(time) {
            delay(500)
            return if (nesting >= nestCountLimit) {
                null
            } else {
                getStartEmbedFromMap(time, nesting + 1, nestCountLimit)
            }
        }
    }

    override fun onTrackEnd(player: AudioPlayer, track: AudioTrack, endReason: AudioTrackEndReason) {
        //logger.debug("track ended eventStartNext:" + endReason.mayStartNext)
        if (endReason.mayStartNext) {
            nextTrack(track)
        }
    }

    fun clear() {
        tracks.clear()
    }

    fun getPosition(audioTrack: AudioTrack): Int =
        if (iPlayer.playingTrack == audioTrack) {
            0
        } else {
            tracks.toList().indexOf(audioTrack) + 1
        }

    fun stop() {
        iPlayer.stopTrack()
    }

    fun skip(amount: Int) {
        var nextTrack: AudioTrack? = null
        for (i in 0 until amount) {
            nextTrack = tracks.poll()
        }
        if (nextTrack == null) {
            stop()
        } else {
            iPlayer.stopTrack()
            iPlayer.playTrack(nextTrack)
        }
    }

    fun setPaused(paused: Boolean) {
        iPlayer.isPaused = paused
    }

    fun removeAt(indexes: IntArray): Map<Int, AudioTrack> {
        val newQueue = LinkedList<AudioTrack>()
        val removed = HashMap<Int, AudioTrack>()

        tracks.forEachIndexed { index, track ->
            if (!indexes.contains(index)) {
                newQueue.add(track)
            } else {
                removed[index] = track
            }
        }
        tracks = newQueue
        return removed
    }

    private fun getAndCheckGuild(): Guild? {
        val guild = MelijnBot.shardManager.getGuildById(guildId)
        if (guild == null) {
            stop()
            lavaManager.closeConnection(guildId)
        }
        return guild
    }
}