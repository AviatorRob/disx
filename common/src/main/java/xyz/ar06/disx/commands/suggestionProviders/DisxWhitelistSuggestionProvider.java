package xyz.ar06.disx.commands.suggestionProviders;

import xyz.ar06.disx.DisxServerAudioRegistry;
import xyz.ar06.disx.config.DisxConfigHandler;
import xyz.ar06.disx.utils.DisxUUIDUtil;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DisxWhitelistSuggestionProvider {
    public static CompletableFuture<Suggestions> getRemovalSuggestions(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        List<Object> list = DisxConfigHandler.SERVER.getUseWhitelist();
        for (Object o : list) {
            try {
                String username = DisxUUIDUtil.getUsernameFromUuid(UUID.fromString(o.toString()));
                suggestionsBuilder.suggest(username);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return suggestionsBuilder.buildFuture();
    }

    public static CompletableFuture<Suggestions> getAdditionSuggestions(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        for (Player player : DisxServerAudioRegistry.getMcPlayers()){
            suggestionsBuilder.suggest(player.getName().getString());
        }
        return suggestionsBuilder.buildFuture();
    }
}