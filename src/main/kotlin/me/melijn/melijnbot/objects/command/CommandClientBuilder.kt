package me.melijn.melijnbot.objects.command

import me.melijn.melijnbot.Container
import me.melijn.melijnbot.commands.administration.*
import me.melijn.melijnbot.commands.animal.*
import me.melijn.melijnbot.commands.anime.*
import me.melijn.melijnbot.commands.developer.*
import me.melijn.melijnbot.commands.image.*
import me.melijn.melijnbot.commands.moderation.*
import me.melijn.melijnbot.commands.music.*
import me.melijn.melijnbot.commands.utility.*
import org.slf4j.LoggerFactory


class CommandClientBuilder(private val container: Container) {

    private val logger = LoggerFactory.getLogger(this::class.java.name)

    private val commands = hashSetOf(
        PunishmentCommand(),
        SetCommandStateCommand(),
        PunchCommand(),
        ColorCommand(),
        LoopCommand(),
        ShuffleCommand(),
        KickCommand(),
        SetLogChannelCommand(),
        KissCommand(),
        MirrorCommand(),
        LickCommand(),
        NyancatCommand(),
        SPlayCommand(),
        ForceRoleCommand(),
        TestCommand(),
        BiteCommand(),
        SetChannelCommand(),
        VolumeCommand(),
        GreyScaleCommand(),
        InfoCommand(),
        GifInfoCommand(),
        FilterCommand(),
        TempBanCommand(),
        DabCommand(),
        LoopQueueCommand(),
        HelpCommand(),
        PrefixesCommand(),
        MuteCommand(),
        StatsCommand(),
        CatCommand(),
        KoalaCommand(),
        ResumeCommand(),
        SoftBanCommand(),
        TrackInfoCommand(),
        RawCommand(),
        BirdCommand(),
        AvatarCommand(),
        SetCooldownCommand(),
        MetricsCommand(),
        UnmuteCommand(),
        PurgeCommand(),
        SeekCommand(),
        VoteCommand(),
        SetVerificationTypeCommand(),
        PlayCommand(),
        SetMusicChannelCommand(),
        FilterGroupCommand(),
        JoinMessageCommand(),
        BlushCommand(),
        LeaveMessageCommand(),
        StareCommand(),
        TempMuteCommand(),
        SummonCommand(),
        VerifyCommand(),
        RewindCommand(),
        SetSlowModeCommand(),
        GreetCommand(),
        BanCommand(),
        InvertCommand(),
        ShutdownCommand(),
        UrbanCommand(),
        HistoryCommand(),
        RoleInfoCommand(),
        AwooCommand(),
        AlpacaCommand(),
        PermissionCommand(),
        WarnCommand(),
        T2eCommand(),
        FlipCommand(),
        ClearChannelCommand(),
        ServerInfo(),
        PatCommand(),
        SelfRoleCommand(),
        PingCommand(),
        SmoothPixelateCommand(),
        HighfiveCommand(),
        InviteCommand(),
        BlurCommand(),
        SetEmbedColorCommand(),
        PandaCommand(),
        CuddleCommand(),
        SlapCommand(),
        EmoteCommand(),
        ForwardCommand(),
        BlurpleCommand(),
        SetMaxUserVerificationFlowRateCommand(),
        UnicodeCommand(),
        UnbanCommand(),
        SpookifyCommand(),
        HugCommand(),
        SetPrivateEmbedColorCommand(),
        StopCommand(),
        RolesCommand(),
        VoteInfoCommand(),
        FoxCommand(),
        PixelateCommand(),
        TickleCommand(),
        SetStreamUrlCommand(),
        SharpenCommand(),
        ThinkingCommand(),
        HandholdingCommand(),
        ShardsCommand(),
        SayCommand(),
        ShootCommand(),
        SettingsCommand(),
        RemoveCommand(),
        PokeCommand(),
        PauseCommand(),
        QueueCommand(),
        PrivatePrefixesCommand(),
        MeguminCommand(),
        NowPlayingCommand(),
        SetBandCommand(),
        SetEmbedStateCommand(),
        LewdCommand(),
        ShrugCommand(),
        PunishmentGroupCommand(),
        PoutCommand(),
        EvalCommand(),
        OwOCommand(),
        SmugCommand(),
        SetVerificationPasswordCommand(),
        DonateCommand(),
        CryCommand(),
        DiscordMemeCommand(),
        SetRoleCommand(),
        UserInfoCommand(),
        SetLanguageCommand(),
        SkipCommand(),
        SetVerificationEmotejiCommand(),
        DogCommand(),
        CustomCommandCommand(),
        ThumbsupCommand(),
        NekoCommand(),
        SetPrivateLanguageCommand(),
        SetBirthdayCommand(),
        SetPrivateTimeZoneCommand(),
        SetTimeZoneCommand(),
        BirthdayMessageCommand(),
        SpamCommand(),
        GainProfileCommand(),
        MusicNodeCommand(),
        DuckCommand(),
        AIWaifuCommand(),
        SetMusic247Command(),
        PreVerificationJoinMessageCommand(),
        PreVerificationLeaveMessageCommand(),
        SupportCommand(),
        VoteSkipCommand(),
        RerenderGifCommand(),
        LyricsCommand(),
        AppendReverseGifCommand(),
        GlobalRecolorCommand(),
        MoveCommand(),
        JoinRoleCommand(),
        MassMoveCommand(),
        LimitRoleToChannelCommand(),
        BannedMessageCommand(),
        KickedMessageCommand(),
        SetBannedOrKickedTriggersLeaveCommand(),
        MyAnimeListCommand(),
        SetBotLogStateCommand(),
        SetRoleColorCommand(),
        AniListCommand(),
        AngryCommand(),
        PngsFromGif(),
        PngsToGif()
    )

    init {
        container.daoManager.commandWrapper.clearCommands()
    }

    fun build(): CommandClient {
        return CommandClient(commands.toSet(), container)
    }

    private suspend fun addCommand(command: AbstractCommand): CommandClientBuilder {
        commands.add(command)
        container.daoManager.commandWrapper.insert(command)
        return this
    }

    suspend fun loadCommands(): CommandClientBuilder {
        logger.info("Loading ${commands.size} commands...")
        commands.forEach { command ->
            addCommand(command)
        }
        logger.info("Loaded ${commands.size} commands")
        return this
    }
}