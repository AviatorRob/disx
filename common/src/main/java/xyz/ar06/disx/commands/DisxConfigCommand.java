package xyz.ar06.disx.commands;

import dev.architectury.platform.Platform;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Style;
import xyz.ar06.disx.DisxAudioStreamingNode;
import xyz.ar06.disx.DisxLogger;
import xyz.ar06.disx.commands.suggestionProviders.DisxBlacklistSuggestionProvider;
import xyz.ar06.disx.commands.suggestionProviders.DisxWhitelistSuggestionProvider;
import xyz.ar06.disx.utils.DisxUUIDUtil;
import xyz.ar06.disx.commands.suggestionProviders.DisxPropertyTypeSuggestionProvider;
import xyz.ar06.disx.config.DisxConfigHandler;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.architectury.event.events.common.CommandRegistrationEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DisxConfigCommand {
    public static void registerCommand(){
        CommandRegistrationEvent.EVENT.register((dispatcher, registry, selection) -> {
            dispatcher.register(Commands.literal("disxconfig")
                            .requires(commandSourceStack -> commandSourceStack.hasPermission(1))
                    .then(Commands.literal("setProperty")
                            .then(Commands.argument("property", StringArgumentType.string()).suggests(DisxPropertyTypeSuggestionProvider::getSuggestions)
                                    .then(Commands.argument("propertyValue", StringArgumentType.string())
                                            .executes(DisxConfigCommand::runSetProperty))))
                    .then(Commands.literal("getProperty")
                            .then(Commands.argument("property", StringArgumentType.string()).suggests(DisxPropertyTypeSuggestionProvider::getSuggestions)
                                    .executes(DisxConfigCommand::runGetProperty)))
                    .then(Commands.literal("getUseWhitelist")
                            .executes(DisxConfigCommand::runGetUseWhitelist))
                    .then(Commands.literal("modifyUseWhitelist")
                            .then(Commands.literal("add")
                                    .then(Commands.argument("username", StringArgumentType.string())
                                            .suggests(DisxWhitelistSuggestionProvider::getAdditionSuggestions)
                                            .executes(DisxConfigCommand::runAddUseWhitelist)))
                            .then(Commands.literal("remove")
                                    .then(Commands.argument("username", StringArgumentType.string())
                                            .suggests(DisxWhitelistSuggestionProvider::getRemovalSuggestions)
                                            .executes(DisxConfigCommand::runRemoveUseWhitelist))))
                    .then(Commands.literal("getUseBlacklist")
                            .executes(DisxConfigCommand::runGetUseBlacklist))
                    .then(Commands.literal("modifyUseBlacklist")
                            .then(Commands.literal("add")
                                    .then(Commands.argument("username", StringArgumentType.string())
                                            .suggests(DisxBlacklistSuggestionProvider::getAdditionSuggestions)
                                            .executes(DisxConfigCommand::runAddUseBlacklist)))
                            .then(Commands.literal("remove")
                                    .then(Commands.argument("username", StringArgumentType.string())
                                            .suggests(DisxBlacklistSuggestionProvider::getRemovalSuggestions)
                                            .executes(DisxConfigCommand::runRemoveUseBlacklist))))
                    .then(Commands.literal("getDimensionBlacklist")
                            .executes(DisxConfigCommand::runGetDimensionBlacklist))
                    .then(Commands.literal("modifyDimensionBlacklist")
                            .then(Commands.literal("add")
                                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                                            .executes(DisxConfigCommand::runAddDimensionBlacklist)))
                            .then(Commands.literal("remove")
                                    .then(Commands.argument("dimension", DimensionArgument.dimension())
                                            .executes(DisxConfigCommand::runRemoveDimensionBlacklist))))
                    .then(Commands.literal("reload")
                            .executes(DisxConfigCommand::runConfigReload))
            );
        });
    }

    private static int runSetProperty(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
        } else {
            String property = context.getArgument("property", String.class);
            String value = context.getArgument("propertyValue", String.class);
            if (DisxConfigHandler.SERVER.getProperty(property) == null || property.equals("refresh_token")){
                context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd_invalid_property"));
            } else {
                DisxConfigHandler.SERVER.updateProperty(property, value);
                context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.configcmd_property_changed", property, value));
            }
        }
        return 1;
    }

    private static int runGetProperty(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
        } else {
            String property = context.getArgument("property", String.class);
            String value = DisxConfigHandler.SERVER.getProperty(property);
            if (value == null || property.equals("refresh_token")){
                context.getSource().sendFailure(Component.literal("Invalid property provided!"));
            } else {
                context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.configcmd_property_value", property, value));
            }
        }
        return 1;
    }

    private static int runGetUseWhitelist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
        } else {
            CompletableFuture.runAsync(() -> {
                List<Object> list = DisxConfigHandler.SERVER.getUseWhitelist();
                ArrayList<Component> toSend = new ArrayList<>();
                toSend.add(Component.translatable("sysmsg.disx.configcmd_whitelisted_players").withStyle(ChatFormatting.BOLD));
                toSend.add(Component.translatable("sysmsg.disx.configcmd_whitelist_status", DisxConfigHandler.SERVER.getProperty("player_use_whitelist_enabled")).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
                for (Object o : list){
                    String name = "";
                    try{
                        DisxLogger.debug("TRYING TO CONVERT STRING TO UUID: " + o.toString());
                        name = DisxUUIDUtil.getUsernameFromUuid(UUID.fromString(o.toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd_whitelist_load_err"));
                        return;
                    }
                    toSend.add(Component.literal("- " + name));
                }
                if (list.isEmpty()){
                    toSend.add(Component.literal("NONE").withStyle(ChatFormatting.ITALIC));
                }
                for (Component c : toSend){
                    context.getSource().sendSystemMessage(c);
                }
            });
        }
        return 1;
    }

    private static int runAddUseWhitelist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
        } else {
            String username = context.getArgument("username", String.class);
            CompletableFuture.runAsync(() -> DisxConfigHandler.SERVER.addToUseWhitelist(username, context));
        }
        return 1;
    }

    private static int runRemoveUseWhitelist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
        } else {
            String username = context.getArgument("username", String.class);
            DisxConfigHandler.SERVER.removeFromUseWhitelist(username, context);
        }
        return 1;
    }

    private static int runGetUseBlacklist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
        } else {
            CompletableFuture.runAsync(() -> {
                List<Object> list = DisxConfigHandler.SERVER.getUseBlacklist();
                ArrayList<Component> toSend = new ArrayList<>();
                toSend.add(Component.translatable("sysmsg.disx.configcmd_blacklisted_players").withStyle(ChatFormatting.BOLD));
                for (Object o : list){
                    String name = "";
                    try{
                        name = DisxUUIDUtil.getUsernameFromUuid(UUID.fromString(o.toString()));
                    } catch (Exception e) {
                        e.printStackTrace();
                        context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd_blacklist_load_err"));
                        return;
                    }
                    toSend.add(Component.literal("- " + name));
                }
                if (list.isEmpty()){
                    toSend.add(Component.literal("NONE").withStyle(ChatFormatting.ITALIC));
                }
                for (Component c : toSend){
                    context.getSource().sendSystemMessage(c);
                }
            });
        }
        return 1;
    }

    private static int runAddUseBlacklist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
        } else {
            String username = context.getArgument("username", String.class);
            CompletableFuture.runAsync(() -> DisxConfigHandler.SERVER.addToUseBlacklist(username, context));
        }
        return 1;
    }

    private static int runRemoveUseBlacklist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
        } else {
            String username = context.getArgument("username", String.class);
            CompletableFuture.runAsync(() -> DisxConfigHandler.SERVER.removeFromUseBlacklist(username, context));
        }
        return 1;
    }

    private static int runGetDimensionBlacklist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
        } else {
            List<Object> list = DisxConfigHandler.SERVER.getDimensionBlacklist();
            ArrayList<Component> toSend = new ArrayList<>();
            toSend.add(Component.translatable("sysmsg.disx.configcmd_dims_blacklisted").withStyle(ChatFormatting.BOLD));
            for (Object o : list){
                toSend.add(Component.literal("- " + o.toString()));
            }
            if (list.isEmpty()){
                toSend.add(Component.literal("NONE").withStyle(ChatFormatting.ITALIC));
            }
            for (Component c : toSend){
                context.getSource().sendSystemMessage(c);
            }
        }
        return 1;
    }

    private static int runAddDimensionBlacklist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
        } else {
            ResourceLocation dimensionLocation = context.getArgument("dimension", ResourceLocation.class);
            String result = DisxConfigHandler.SERVER.addToDimensionBlacklist(dimensionLocation);
            if (result.equals("success")){
                context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.configcmd_dim_blacklist_modified_add", dimensionLocation.toString()));
            } else if (result.equals("failure")){
                context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd.dim_blacklist_modify_add_err"));
            } else if (result.equals("duplicate")){
                context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd.dim_blacklist_modify_add_dupe"));
            }
        }
        return 1;
    }

    private static int runRemoveDimensionBlacklist(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
        } else {
            ResourceLocation dimensionLocation = context.getArgument("dimension", ResourceLocation.class);
            String result = DisxConfigHandler.SERVER.removeFromDimensionBlacklist(dimensionLocation);
            if (result.equals("success")){
                context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.configcmd_dim_blacklist_modified_remove", dimensionLocation.toString()));
            } else if (result.equals("failure")){
                context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd.dim_blacklist_modify_add_err"));
            } else if (result.equals("notfoundonit")){
                context.getSource().sendFailure(Component.translatable("sysmsg.disx.configcmd.dim_blacklist_modify_remove_err"));
            }
        }
        return 1;
    }

    private static int runConfigReload(CommandContext<CommandSourceStack> context){
        if (!context.getSource().hasPermission(1)){
            context.getSource().sendFailure(Component.translatable("sysmsg.disx.cmd_no_permission"));
        } else {
            DisxConfigHandler.SERVER.initializeConfig(context.getSource().getServer());
            context.getSource().sendSystemMessage(Component.translatable("sysmsg.disx.configcmd.config_reloaded"));
        }
        return 1;
    }

}
