package xyz.ar06.disx.commands;

import xyz.ar06.disx.DisxSystemMessages;
import xyz.ar06.disx.config.DisxConfigHandler;
import xyz.ar06.disx.items.DisxRecordStamp;
import xyz.ar06.disx.utils.DisxYoutubeResolver;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class DisxStampCommand {

    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register(((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("disxstamp")
                            .requires(commandSourceStack -> commandSourceStack.hasPermission(2))
                    .then(Commands.argument("player", EntityArgument.players())
                            .then(Commands.argument("videoId", StringArgumentType.string())
                                    .executes(DisxStampCommand::run))));
        }));
    }
    private static int run(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
            return 1;
        } else {
            context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.stampcmd.one_moment"));
            CompletableFuture.runAsync(() -> runAsync(context));
        }
        return 1;
    }

    private static void runAsync(CommandContext<CommandSourceStack> context){
        String videoId = context.getArgument("videoId", String.class);
        Collection<ServerPlayer> playerCollection;
        try {
            playerCollection = EntityArgument.getPlayers(context, "player");
        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.stampcmd.no_player_err"));
            throw new RuntimeException(e);
        }
        ArrayList<String> title_and_length = DisxYoutubeResolver.scrapeLengthAndTitle(videoId);

        if (title_and_length == null && DisxConfigHandler.SERVER.getProperty("video_existence_check").equals("true")) {
            if (context.getSource().isPlayer()){
                DisxSystemMessages.noVideoFound(context.getSource().getPlayer());
            } else {
                DisxSystemMessages.noVideoFound(context.getSource().getServer());
            }
            return;
        }
        String videoName = title_and_length.get(0);
        int videoLength = Integer.valueOf(title_and_length.get(1));
        if (videoLength > 1800) {
            if (context.getSource().isPlayer()){
                DisxSystemMessages.badDuration(context.getSource().getPlayer());
            } else {
                DisxSystemMessages.badDuration(context.getSource().getServer());
            }
            return;
        }
        Item item = DisxRecordStamp.getItemRegistration().get();
        ItemStack recordStampStack = new ItemStack(item);
        recordStampStack.setCount(1);
        CompoundTag tag = new CompoundTag();
        tag.putString("videoId", videoId);
        tag.putString("videoName", videoName);
        recordStampStack.setTag(tag);
        for (ServerPlayer plr : playerCollection){
            plr.playNotifySound(SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.MASTER, 1, 1);
            plr.playNotifySound(SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.MASTER, 1f, 1f);
            plr.addItem(recordStampStack);
        }
        context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.stampcmd.success"));
    }
}
