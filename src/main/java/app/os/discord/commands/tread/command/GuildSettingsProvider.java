package app.os.discord.commands.tread.command;

import javax.annotation.Nullable;
import java.util.Collection;

public interface GuildSettingsProvider {

    @Nullable
    default Collection<String> getPrefixes() {
        return null;
    }
}
