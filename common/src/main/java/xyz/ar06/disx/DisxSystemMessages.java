package xyz.ar06.disx;

import dev.architectury.event.events.common.PlayerEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import xyz.ar06.disx.config.DisxConfigHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DisxSystemMessages {
    public static void onPlayReadyNoInternet(Player player) {
        player.sendSystemMessage(Component.translatable("sysmsg.disx.no_internet").withStyle(ChatFormatting.RED));
    }

    public static void noInternetErrorMessage(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.translatable("sysmsg.disx.no_internet").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void noInternetErrorMessage(MinecraftServer server){
        server.sendSystemMessage(Component.literal("Disx Error: No internet connection!").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void errorLoading(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.translatable("sysmsg.disx.error_loading_vid").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void errorLoading(LocalPlayer player){
        player.sendSystemMessage(Component.translatable("sysmsg.disx.error_loading_vid").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void apiError(LocalPlayer player){
        player.sendSystemMessage(Component.translatable("sysmsg.disx.error_api_response").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void noVideoFound(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.translatable("sysmsg.disx.error_no_match").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void noVideoFound(MinecraftServer server){
        server.sendSystemMessage(Component.literal("Disx Error: No video found!").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void noVideoFound(LocalPlayer player){
        player.sendSystemMessage(Component.translatable("sysmsg.disx.error_no_match").withStyle(Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.RED))));
    }

    public static void playingAtLocation(MinecraftServer server, String playerName, BlockPos pos, String videoId, ResourceLocation dimension){
        server.sendSystemMessage(Component.literal("" + playerName + " is playing Video '" + videoId +"' at " + pos.toString() + " in " + dimension.toString()));
    }

    public static void invalidDiscType(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.translatable("sysmsg.disx.error_invalid_disc_type").withStyle(ChatFormatting.RED));
    }

    public static void invalidDiscType(MinecraftServer server){
        server.sendSystemMessage(Component.literal("Disx Error: Invalid disc type provided!").withStyle(ChatFormatting.RED));
    }

    public static void blacklistedByServer(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.translatable("sysmsg.disx.error_blacklisted").withStyle(ChatFormatting.RED));
    }

    public static void notWhitelistedByServer(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.translatable("sysmsg.disx.error_not_whitelisted").withStyle(ChatFormatting.RED));
    }

    public static void dimensionBlacklisted(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.translatable("sysmsg.disx.error_dimension_blacklisted").withStyle(ChatFormatting.RED));
    }

    public static void maxAudioPlayerCtReached(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.translatable("sysmsg.disx.error_max_audio_count").withStyle(ChatFormatting.RED));
    }

    public static void maxAudioPlayerCtReached(MinecraftServer server){
        server.sendSystemMessage(Component.literal("Disx Error: Maximum number of audio instances defined in disx server config has been reached!").withStyle(ChatFormatting.RED));
    }

    public static void noInternetFoundStampMakerAsync(MinecraftServer server, BlockPos blockPos){
        server.sendSystemMessage(Component.literal("Disx Error: Tried to make Record Stamp at " + blockPos.toString() + " but no internet connection has been found!"));
    }

    public static void videoNotFoundStampMakerAsync(MinecraftServer server, BlockPos blockPos){
        server.sendSystemMessage(Component.literal("Disx Error: Tried to make Record Stamp at " + blockPos.toString() + " but no video has been found for provided video id!"));
    }

    public static void badDuration(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.translatable("sysmsg.disx.error_bad_duration", "30").withStyle(ChatFormatting.RED));
    }

    public static void badDuration(MinecraftServer server){
        server.sendSystemMessage(Component.literal("Disx Error: Video length too long! (Max Length: 30 min)").withStyle(ChatFormatting.RED));
    }

    public static void badDurationStampMakerAsync(MinecraftServer server, BlockPos blockPos){
        server.sendSystemMessage(Component.literal("Disx Error: Tried to make Record Stamp at " + blockPos.toString() + " but video length was too long! (Max Length: 30m)"));
    }


    public static void ageRestrictionEnabled(Player player){
        ServerPlayer serverPlayer = (ServerPlayer) player;
        serverPlayer.sendSystemMessage(Component.literal("Disx Error: This video is age restricted, and age restricted playback is not allowed on this server!").withStyle(ChatFormatting.RED));
    }

    public static void loadingVideo(String videoId){
        sendOverlayMessage(Component.literal("Loading Video '" + videoId + "'").withStyle(ChatFormatting.ITALIC), false);
    }

    public static void playingVideo(String videoId){
        Minecraft.getInstance().gui.setNowPlaying(Component.translatable("sysmsg.disx.playing_video", videoId));
    }

    public static void sendOverlayMessage(MutableComponent mutableComponent, boolean animated){
        Minecraft.getInstance().gui.setOverlayMessage(mutableComponent, animated);
    }
    public static void loopStatusMsg(boolean enabled){
        MutableComponent enabledMessage = Component.translatable("sysmsg.disx.loop_enabled").withStyle(ChatFormatting.GRAY);
        MutableComponent disabledMessage = Component.translatable("sysmsg.disx.loop_disabled").withStyle(ChatFormatting.GRAY);
        MutableComponent toSendMessage = enabled ? enabledMessage : disabledMessage;
        sendOverlayMessage(toSendMessage, false);
    }

    public static void pauseStatusMsg(boolean paused){
        MutableComponent enabledMessage = Component.translatable("sysmsg.disx.paused_audio").withStyle(ChatFormatting.GRAY);
        MutableComponent disabledMessage = Component.translatable("sysmsg.disx.resumed_audio").withStyle(ChatFormatting.GRAY);
        MutableComponent toSendMessage = paused ? enabledMessage : disabledMessage;
        sendOverlayMessage(toSendMessage, false);
    }

    public static void mutedAlready(){
        Minecraft.getInstance().player.sendSystemMessage(Component.translatable("sysmsg.disx.mutecmd.error").withStyle(ChatFormatting.RED));
    }

    public static void notMuted(){
        Minecraft.getInstance().player.sendSystemMessage(Component.translatable("sysmsg.disx.unmutecmd.error").withStyle(ChatFormatting.RED));
    }

    public static void successfulMutation(){
        Minecraft.getInstance().player.sendSystemMessage(Component.translatable("sysmsg.disx.mutecmd.success"));
    }

    public static void successfulUnmutation(){
        Minecraft.getInstance().player.sendSystemMessage(Component.translatable("sysmsg.disx.unmutecmd.success"));
    }

    public static void nodeAtLocation(MinecraftServer server){
        server.sendSystemMessage(Component.literal("Disx Error: There is already audio playing at that location!"));
    }

    public static void nodeAtLocation(ServerPlayer player){
        player.sendSystemMessage(Component.translatable("sysmsg.disx.soundcmd.already_node_there"));
    }

    public static void debugStatus(boolean b){
        if (b){
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("sysmsg.disx.debug_enabled"));
            Minecraft.getInstance().player.playSound(SoundEvents.LIGHTNING_BOLT_THUNDER);
        } else {
            Minecraft.getInstance().player.sendSystemMessage(Component.translatable("sysmsg.disx.debug_disabled"));
            Minecraft.getInstance().player.playSound(SoundEvents.TOTEM_USE);
        }
    }

    static List<TextColor> volMsgColors = List.of(
            TextColor.fromRgb(0x00FF00),
            TextColor.fromRgb(0x00E600),
            TextColor.fromRgb(0x00CD00),
            TextColor.fromRgb(0x00B400),
            TextColor.fromRgb(0x009B00),
            TextColor.fromRgb(0x008200),
            TextColor.fromRgb(0x006900),
            TextColor.fromRgb(0x005000),
            TextColor.fromRgb(0x003700),
            TextColor.fromRgb(0x000500)
    );
    static TextColor volMsgEmptyColor = TextColor.fromRgb(0x000000);

    public static void volumeSetMessage(int volume){
        if (volume > 200){
            volume = 200;
        }
        int length = volume / 10;
        if (length > 10){
            length = 10;
        }
        MutableComponent message = Component.literal("VOL: [").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD);
        int index = length - 1;
        for (int i = 0; i < 10; i++){
            if (index >= 0){
                message.append(Component.literal("▮").withStyle(Style.EMPTY.withColor(volMsgColors.get(index))));
                index--;
            } else {
                message.append(Component.literal("▮").withStyle(Style.EMPTY.withColor(volMsgEmptyColor)));
            }
        }
        message.append(Component.literal("] (").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        message.append(Component.literal(String.valueOf(volume)));
        message.append(Component.literal("%)").withStyle(ChatFormatting.WHITE, ChatFormatting.BOLD));
        sendOverlayMessage(message, false);
    }

    public static void refreshTokenGenerated(CommandSourceStack commandSourceStack){
        commandSourceStack.sendSystemMessage(Component.translatable("sysmsg.disx.configcmd.generatedrefreshtoken"));
    }

    public static void signInError(ServerPlayer serverPlayer){
        serverPlayer.sendSystemMessage(Component.translatable("sysmsg.disx.error_oauth")
                .withStyle(ChatFormatting.DARK_RED));
    }

    public static void status403Error(ServerPlayer serverPlayer){
        serverPlayer.sendSystemMessage(Component.translatable("sysmsg.disx.error_stat403")
                .withStyle(ChatFormatting.DARK_RED));
    }

    public static void outdatedModVersion(MinecraftServer server){
        if (!DisxModInfo.getIsUpToDate()){
            ArrayList<MutableComponent> messages = new ArrayList<MutableComponent>();
            messages.add(Component.translatable("sysmsg.disx.notice_outdated").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
            messages.add(Component.translatable("sysmsg.disx.outdated_version_info", DisxModInfo.getVERSION(), DisxModInfo.getLatestVersion()).withStyle(ChatFormatting.GRAY));
            messages.add(Component.translatable("sysmsg.disx.outdated_version_count", DisxModInfo.getVersionsOutdated()).withStyle(ChatFormatting.ITALIC));
            messages.add(Component.translatable("sysmsg.disx.outdated_consider_updating"));
            messages.add(Component.translatable("sysmsg.disx.outdated_run_disxinfo"));
            if (server.isSingleplayer()){
                PlayerEvent.PLAYER_JOIN.register(player -> {
                    for (MutableComponent component : messages){
                        player.sendSystemMessage(component);
                    }
                    player.sendSystemMessage(Component.empty());
                });
            } else {
                for (MutableComponent component : messages){
                    server.sendSystemMessage(component);
                }
                PlayerEvent.PLAYER_JOIN.register(player -> {
                    if (player.hasPermissions(1)){
                        for (MutableComponent component : messages){
                            player.sendSystemMessage(component);
                        }
                        player.sendSystemMessage(Component.empty());
                    }
                });
            }
        }
    }

    public static void potentialModConflict(MinecraftServer server) {
        String instructionsURL_CARRYON = DisxModInfo.getCarryonConfigInstructionsUrl();

        HashMap<String, Boolean> conflicts = DisxModInfo.getPotentialModConflicts();
        boolean oneConflictFound = false;
        ArrayList<MutableComponent> messages = new ArrayList<MutableComponent>();
        for (Object o : conflicts.keySet()) {
            if (conflicts.get(o).equals(true)) {
                if (oneConflictFound == false) {
                    oneConflictFound = true;
                    messages.add(Component.translatable("sysmsg.disx.notice_potential_conflict").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD));
                }
                if (o.toString().equals("carryon")) {
                    MutableComponent instructionsLink = Component.literal("here").withStyle(ChatFormatting.UNDERLINE).withStyle(ChatFormatting.DARK_AQUA).withStyle(Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, instructionsURL_CARRYON)));
                    if (server.isDedicatedServer()){
                        instructionsLink = instructionsLink.append(Component.literal(" (" + instructionsURL_CARRYON + ")"));
                    }
                    messages.add(Component.translatable("sysmsg.disx.conflict_carryon", instructionsLink));
                }
            }
        }
        if (server.isSingleplayer()){
            PlayerEvent.PLAYER_JOIN.register(player -> {
                for (MutableComponent component : messages){
                    player.sendSystemMessage(component);
                }
                if (!messages.isEmpty()){
                    player.sendSystemMessage(Component.empty());
                }

            });
        } else {
            for (MutableComponent component : messages){
                server.sendSystemMessage(component);
            }
            PlayerEvent.PLAYER_JOIN.register(player -> {
                if (player.hasPermissions(1)){
                    for (MutableComponent component : messages){
                        player.sendSystemMessage(component);
                    }
                    if (!messages.isEmpty()){
                        player.sendSystemMessage(Component.empty());
                    }
                }
            });
        }
    }

    public static void devBuildNotice(MinecraftServer server){
        if (DisxModInfo.getIsDevBuild()){
            MutableComponent message = Component.translatable("sysmsg.disx.notice_devbuild")
                    .withStyle(ChatFormatting.GOLD)
                    .withStyle(ChatFormatting.BOLD);
            MutableComponent message2 = Component.translatable("sysmsg.disx.devbuild_installed_version", DisxModInfo.getVERSION())
                    .withStyle(ChatFormatting.GRAY);
            if (server.isSingleplayer()){
                PlayerEvent.PLAYER_JOIN.register(player -> {
                    player.sendSystemMessage(message);
                    player.sendSystemMessage(message2);
                    player.sendSystemMessage(Component.empty());
                });
            } else {
                server.sendSystemMessage(message);
                server.sendSystemMessage(message2);
                PlayerEvent.PLAYER_JOIN.register(player -> {
                    if (player.hasPermissions(1)){
                        player.sendSystemMessage(message);
                        player.sendSystemMessage(message2);
                        player.sendSystemMessage(Component.empty());
                    }
                });
            }
        }

    }

    @Deprecated public static void forcingLiveYtSrc(MinecraftServer server){
        boolean configuredForLiveSrc = Boolean.parseBoolean(DisxConfigHandler.SERVER.getProperty("use_live_ytsrc"));
        if (DisxModInfo.isForceLiveytsrc() && !configuredForLiveSrc){
            MutableComponent message = Component.translatable("sysmsg.disx.notice_forced_disxlivesrc")
                    .withStyle(ChatFormatting.RED)
                    .withStyle(ChatFormatting.BOLD);
            if (server.isSingleplayer()){
                PlayerEvent.PLAYER_JOIN.register(player -> {
                    player.sendSystemMessage(message);
                    player.sendSystemMessage(Component.empty());
                });
            } else {
                server.sendSystemMessage(message);
                PlayerEvent.PLAYER_JOIN.register(player -> {
                    if (player.hasPermissions(1)){
                        player.sendSystemMessage(message);
                        player.sendSystemMessage(Component.empty());
                    }
                });
            }
        }

    }

    @Deprecated public static void forcingDisxYtSrcApi(MinecraftServer server){
        boolean configuredForLiveSrc = Boolean.parseBoolean(DisxConfigHandler.SERVER.getProperty("use_live_ytsrc"));
        if (DisxModInfo.isForceDisxytsrcapi() && configuredForLiveSrc){
            MutableComponent message = Component.translatable("sysmsg.disx.notice_forced_disxytsrcapi")
                    .withStyle(ChatFormatting.RED)
                    .withStyle(ChatFormatting.BOLD);
            if (server.isSingleplayer()){
                PlayerEvent.PLAYER_JOIN.register(player -> {
                    player.sendSystemMessage(message);
                    player.sendSystemMessage(Component.empty());
                });
            } else {
                server.sendSystemMessage(message);
                PlayerEvent.PLAYER_JOIN.register(player -> {
                    if (player.hasPermissions(1)){
                        player.sendSystemMessage(message);
                        player.sendSystemMessage(Component.empty());
                    }
                });
            }
        }
    }

}
