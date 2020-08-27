package app.os.discord.commands.creator;

import app.os.discord.DiscordBot;
import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;

public class GetGuild extends Command {
    public GetGuild() {
        this.name = "guildInfo";
        this.requiredRole = DiscordBot.CREATOR_ROLE;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        commandEvent.getChannel().sendMessage(commandEvent.getGuild().toString()).queue();
    }
}