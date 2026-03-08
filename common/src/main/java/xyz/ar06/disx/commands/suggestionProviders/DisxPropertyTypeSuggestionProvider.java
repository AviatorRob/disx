package xyz.ar06.disx.commands.suggestionProviders;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.architectury.platform.Platform;
import net.minecraft.commands.CommandSourceStack;

import java.util.concurrent.CompletableFuture;

public class DisxPropertyTypeSuggestionProvider {
    public static CompletableFuture<Suggestions> getSuggestions(CommandContext<CommandSourceStack> commandSourceStackCommandContext, SuggestionsBuilder suggestionsBuilder) {
        suggestionsBuilder.suggest("player_use_whitelist_enabled");
        suggestionsBuilder.suggest("video_existence_check");
        suggestionsBuilder.suggest("max_audio_players");
        suggestionsBuilder.suggest("debug_mode");
        suggestionsBuilder.suggest("audio_radius");
        suggestionsBuilder.suggest("sound_particles");
        //suggestionsBuilder.suggest("refresh_token");
        //suggestionsBuilder.suggest("age_restricted_playback");
        return suggestionsBuilder.buildFuture();
    }
}
