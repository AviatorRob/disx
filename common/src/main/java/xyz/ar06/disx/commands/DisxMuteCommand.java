package xyz.ar06.disx.commands;

import xyz.ar06.disx.DisxServerPacketIndex;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import dev.architectury.event.events.common.TickEvent;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class DisxMuteCommand {

    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("disxmute")
                    .then(Commands.argument("player", EntityArgument.players())
                            .executes(DisxMuteCommand::run))
            );
        }));
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("disxunmute")
                    .then(Commands.argument("player", EntityArgument.players())
                            .executes(DisxMuteCommand::runUnmute))
            );
        }));
    }
    private static int run(CommandContext<CommandSourceStack> context){
        if (context.getSource().isPlayer()){
            try {
                Player executor = context.getSource().getPlayer();
                Collection<ServerPlayer> playerCollection = EntityArgument.getPlayers(context, "player");
                for (ServerPlayer plr : playerCollection){
                    DisxServerPacketIndex.ServerPackets.mutePlayer(executor, plr.getUUID());
                }
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else {
            context.getSource().sendFailure(Component.literal("Only players can run this command!"));
        }
        return 1;
    }

    private static int runUnmute(CommandContext<CommandSourceStack> context){
        if (context.getSource().isPlayer()){
            try {
                Player executor = context.getSource().getPlayer();
                Collection<ServerPlayer> playerCollection = EntityArgument.getPlayers(context, "player");
                for (ServerPlayer plr : playerCollection){
                    DisxServerPacketIndex.ServerPackets.unmutePlayer(executor, plr.getUUID());
                }
            } catch (CommandSyntaxException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        } else {
            context.getSource().sendFailure(Component.literal("Only players can run this command!"));
        }
        return 1;
    }

}
