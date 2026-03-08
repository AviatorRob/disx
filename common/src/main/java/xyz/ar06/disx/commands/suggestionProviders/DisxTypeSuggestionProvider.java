package xyz.ar06.disx.commands.suggestionProviders;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;

import java.util.concurrent.CompletableFuture;

public class DisxTypeSuggestionProvider {

    public static CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        suggestionsBuilder.suggest("diamond");
        suggestionsBuilder.suggest("gold");
        suggestionsBuilder.suggest("iron");
        suggestionsBuilder.suggest("blue");
        suggestionsBuilder.suggest("green");
        suggestionsBuilder.suggest("orange");
        suggestionsBuilder.suggest("pink");
        suggestionsBuilder.suggest("purple");
        suggestionsBuilder.suggest("red");
        suggestionsBuilder.suggest("yellow");
        suggestionsBuilder.suggest("default");
        return suggestionsBuilder.buildFuture();
    }
}
