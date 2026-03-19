package xyz.ar06.disx.commands;

import com.mojang.brigadier.arguments.BoolArgumentType;
import xyz.ar06.disx.audio_filters.DisxAudioFilterType;
import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.utils.DisxInternetCheck;
import xyz.ar06.disx.DisxServerAudioRegistry;
import xyz.ar06.disx.DisxSystemMessages;
import xyz.ar06.disx.utils.DisxYTDLPWrapper;
import xyz.ar06.disx.config.DisxConfigHandler;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class DisxSoundCommand {

    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            LiteralCommandNode<CommandSourceStack> register = dispatcher.register(Commands.literal("disxsound")
                    .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                    .then(Commands.argument("videoId", StringArgumentType.string())
                            .then(Commands.argument("dimension", DimensionArgument.dimension())
                                    .then(Commands.argument("position", BlockPosArgument.blockPos())
                                            .then(Commands.argument("startTime", IntegerArgumentType.integer())
                                                    .then(Commands.argument("volume", IntegerArgumentType.integer(0, 200))
                                                            .then(Commands.argument("loop", BoolArgumentType.bool())
                                                                    .executes(DisxSoundCommand::run))))))));
        }));
    }

    private static int run(CommandContext<CommandSourceStack> context) {
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
        } else
        {
            if (context.getSource().isPlayer()){
                if (DisxConfigHandler.SERVER.isOnUseBlacklist(context.getSource().getPlayer().getUUID())){
                    DisxSystemMessages.blacklistedByServer(context.getSource().getPlayer());
                    return 1;
                }
                if (!DisxConfigHandler.SERVER.isOnUseWhitelist(context.getSource().getPlayer().getUUID())){
                    DisxSystemMessages.notWhitelistedByServer(context.getSource().getPlayer());
                    return 1;
                }
            }
            int currentAudioPlayerCount = DisxServerAudioRegistry.getRegistryCount();
            int maxAudioPlayerCount = Integer.valueOf(DisxConfigHandler.SERVER.getProperty("max_audio_players"));
            if (currentAudioPlayerCount >= maxAudioPlayerCount){
                if (context.getSource().isPlayer()){
                    DisxSystemMessages.maxAudioPlayerCtReached(context.getSource().getPlayer());
                } else {
                    DisxSystemMessages.maxAudioPlayerCtReached(context.getSource().getServer());
                }
            }
            context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.soundcmd.one_moment"));
            CompletableFuture.runAsync(() -> runAsync(context));
        }
        return 1;
    }

    private static void runAsync(CommandContext<CommandSourceStack> context){
        try {
            String videoId = context.getArgument("videoId", String.class);
            ResourceLocation dimension = context.getArgument("dimension", ResourceLocation.class);
            BlockPos blockPos = BlockPosArgument.getBlockPos(context, "position");
            Integer startTime = context.getArgument("startTime", Integer.class);
            Integer volumePercentage = context.getArgument("volume", Integer.class);
            Boolean loop = context.getArgument("loop", Boolean.class);
            int rogueRadius = -1;
            ArrayList<DisxAudioFilterType> audioFilters = new ArrayList<DisxAudioFilterType>();
            DisxLogger.debug("START TIME PROVIDED: " + startTime);
            boolean hasInternet = DisxInternetCheck.checkInternet();
            if (!hasInternet){
                throw new Exception("No Internet Connection");
            }
            if (DisxServerAudioRegistry.isNodeAtLocation(blockPos, dimension)){
                throw new Exception("Audio At Location");
            }
            int videoLength = DisxYTDLPWrapper.getVideoLength(videoId);
            if (videoLength == -1 && DisxConfigHandler.SERVER.getProperty("video_existence_check").equals("true")){
                throw new Exception("Video Not Found");
            }
            if (videoLength > 1800) {
                throw new Exception("Too Long");
            }
            if (!context.getSource().isPlayer()){
                DisxServerAudioRegistry.addToRegistry(blockPos, videoId, null, context.getSource().getServer(), dimension, startTime.intValue(), loop, volumePercentage, rogueRadius, audioFilters);
            } else {
                DisxServerAudioRegistry.addToRegistry(blockPos, videoId, context.getSource().getPlayer(), context.getSource().getServer(), dimension, startTime.intValue(), loop, volumePercentage, rogueRadius, audioFilters);
            }
            context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.soundcmd.attempting_playback", videoId, blockPos.toString(), dimension.toString()));
        } catch (Exception e){
            if (e.getMessage().equals("Video Not Found")) {
                if (context.getSource().isPlayer()){
                    DisxSystemMessages.noVideoFound(context.getSource().getPlayer());
                } else {
                    DisxSystemMessages.noVideoFound(context.getSource().getServer());
                }
            }
            if (e.getMessage().equals("No Internet Connection")) {
                if (context.getSource().isPlayer()){
                    DisxSystemMessages.noInternetErrorMessage(context.getSource().getPlayer());
                } else {
                    DisxSystemMessages.noInternetErrorMessage(context.getSource().getServer());
                }
            }
            if (e.getMessage().equals("Too Long")){
                if (context.getSource().isPlayer()){
                    DisxSystemMessages.badDuration(context.getSource().getPlayer());
                } else {
                    DisxSystemMessages.badDuration(context.getSource().getServer());
                }
            }
            if (e.getMessage().equals("Audio At Location")){
                if (context.getSource().isPlayer()){
                    DisxSystemMessages.nodeAtLocation(context.getSource().getPlayer());
                } else {
                    DisxSystemMessages.nodeAtLocation(context.getSource().getServer());
                }
            }
        }
    }

}
