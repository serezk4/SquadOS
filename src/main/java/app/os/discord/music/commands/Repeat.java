package app.os.discord.music.commands;

import app.os.discord.DiscordBot;
import app.os.discord.music.self.GuildMusicManager;
import app.os.discord.music.self.MusicManager;
import app.os.discord.commands.self.Command;
import app.os.discord.commands.self.CommandEvent;

public class Repeat extends Command {
    public Repeat() {
        this.name = "repeat";
        this.help = "повторять текущий трек // в разработке";
        this.requiredRole = DiscordBot.DJ_ROLE;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        GuildMusicManager guildMusicManager = MusicManager.getInstance().getGuildAudioPlayer(commandEvent.getGuild());

        boolean repeat = !guildMusicManager.scheduler.isRepeat();
        guildMusicManager.scheduler.setRepeat(repeat);
        commandEvent.getChannel().sendMessage(repeat ? "Повторение включено!" : "Повторение выключено!").queue();
    }
}
