package xyz.ar06.disx.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

public class DisxHelpCommand {

    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            LiteralCommandNode<CommandSourceStack> register = dispatcher.register(
                    Commands.literal("disxhelp")
                            .executes(DisxHelpCommand::run)
            );
        }));
    }

    private static int run(CommandContext<CommandSourceStack> context){
        MutableComponent[] messageLinesNO_OP = {
                Component.literal("Disx Commands:")
                        .withStyle(ChatFormatting.BOLD),
                Component.literal("/disxhelp - ").append(Component.translatable("sysmsg.disx.helpcmd.desc.disxhelp")),
                Component.literal("/disxinfo - ").append(Component.translatable("sysmsg.disx.helpcmd.desc.disxinfo")),
                Component.literal("/disxmute - ").append(Component.translatable("sysmsg.disx.helpcmd.desc.disxmute")),
                Component.translatable("sysmsg.disx.helpcmd.usage.disxmute","/disxmute")
                        .withStyle(ChatFormatting.GRAY),
                Component.literal("/disxunmute - ").append(Component.translatable("sysmsg.disx.helpcmd.desc.disxunmute", "/disxmute")),
                Component.translatable("sysmsg.disx.helpcmd.usage.disxunmute", "/disxunmute")
                        .withStyle(ChatFormatting.GRAY)
        };
        MutableComponent[] messageLinesOP = {
                Component.literal("/disxgen - ").append(Component.translatable("sysmsg.disx.helpcmd.desc.disxgen")),
                Component.translatable("sysmsg.disx.helpcmd.usage.disxgen", "/disxgen")
                        .withStyle(ChatFormatting.GRAY),
                Component.literal("/disxsound - ").append(Component.literal("sysmsg.disx.helpcmd.desc.disxsound")),
                Component.translatable("sysmsg.disx.helpcmd.usage.disxsound", "/disxsound")
                        .withStyle(ChatFormatting.GRAY),
                Component.literal("/disxforcestop - ").append(Component.translatable("sysmsg.disx.helpcmd.desc.disxforcestop")),
                Component.literal("/disxconfig - ").append(Component.translatable("sysmsg.disx.helpcmd.desc.disxconfig")),
                Component.translatable("sysmsg.disx.helpcmd.usage.disxconfig", "/disxconfig")
                        .withStyle(ChatFormatting.GRAY),
                Component.literal("/disxstamp - ").append(Component.translatable("sysmsg.disx.helpcmd.desc.disxstamp")),
                Component.translatable("sysmsg.disx.helpcmd.usage.disxstamp", "/disxstamp")
                        .withStyle(ChatFormatting.GRAY)
        };
        Player player = context.getSource().getPlayer();
        if (player != null){
            if (player.hasPermissions(1)){
                for (MutableComponent component : messageLinesNO_OP) {
                    player.sendSystemMessage(component);
                }
                for (MutableComponent component : messageLinesOP) {
                    player.sendSystemMessage(component);
                }
            } else {
                for (MutableComponent component : messageLinesNO_OP) {
                    player.sendSystemMessage(component);
                }
            }
        } else {
            for (MutableComponent component : messageLinesNO_OP) {
                context.getSource().getServer().sendSystemMessage(component);
            }
            for (MutableComponent component : messageLinesOP) {
                context.getSource().getServer().sendSystemMessage(component);
            }
        }
        return 1;
    }
}
