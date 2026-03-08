package xyz.ar06.disx.commands;

import xyz.ar06.disx.DisxServerAudioRegistry;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class DisxForceStopCommand {
    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("disxforcestop")
                    .requires(commandSourceStack -> commandSourceStack.hasPermission(1))
                    .executes(DisxForceStopCommand::run));
        }));
    }
    private static int run(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.cmd_no_permission").withStyle(ChatFormatting.RED));
            return 1;
        }
        DisxServerAudioRegistry.forceStopAll();
        context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.forcestopcmd.response"));
        return 1;
    }
}
