package app.os.discord.music.commands;

import app.os.discord.DiscordBot;
import app.os.discord.commands.self.Command;
import app.os.discord.commands.self.CommandEvent;
import app.os.discord.music.self.GuildMusicManager;
import app.os.discord.music.self.MusicManager;
import net.dv8tion.jda.api.entities.Message;

import java.util.ArrayList;
import java.util.List;

public class Player extends Command {
    private static final List<UpdateBar> activePlayers = new ArrayList<>();

    public Player() {
        this.name = "player";
        this.help = "открыть плеер";
        this.requiredRole = DiscordBot.DJ_ROLE;
        this.cooldown = 50;
    }

    @Override
    protected void execute(CommandEvent commandEvent) {
        try {
            GuildMusicManager guildMusicManager = MusicManager.getInstance().getGuildAudioPlayer(commandEvent.getGuild());

            commandEvent.getChannel().sendMessage("**Загрузка...**").queue(q -> {
                        UpdateBar bar = new UpdateBar(guildMusicManager, q);

                        activePlayers.forEach(player -> {
                            if (bar.getGuildID() == player.getGuildID())
                                player.stopUpdate();
                        });

                        activePlayers.add(0, bar);

                        bar.start();
                    }
            );

        } catch (NullPointerException e) {
            commandEvent.getChannel().sendMessage("Ничего не воспроизводится!").queue();
        }
    }


    private static class UpdateBar extends Thread {
        private final GuildMusicManager guildMusicManager;
        private final Message toUpdate;
        private boolean active = true;

        public long getGuildID() {
            return toUpdate.getGuild().getIdLong();
        }

        public UpdateBar(GuildMusicManager guildMusicManager, Message toUpdate) {
            setDaemon(true);
            setName("UpdateBar Thread - " + toUpdate.getIdLong());

            this.guildMusicManager = guildMusicManager;
            this.toUpdate = toUpdate;
        }

        public void stopUpdate() {
            toUpdate.editMessage("Плеер закрыт.").queue();
            this.active = false;
            remove();
        }

        private String timeToString(long secs) {
            long hour = secs / 3600,
                    min = secs / 60 % 60,
                    sec = secs % 60;
            return String.format("%02d:%02d:%02d", hour, min, sec);
        }

        private void setButtons(Message message) {
            try {
                message.addReaction("\u23EF").queue(); // ⏯
                message.addReaction("\u23ED").queue(); // ⏭
            } catch (Exception ignored) {
            }
        }

        @Override
        public void run() {
            while (active) {
                try {
                    StringBuilder toSend = new StringBuilder();

                    int startSeconds = 0;
                    int endSeconds = (int) (guildMusicManager.player.getPlayingTrack().getInfo().length / 1000);
                    int nowSeconds = (int) (guildMusicManager.player.getPlayingTrack().getPosition() / 1000);

                    toSend.append("**").append(guildMusicManager.player.getPlayingTrack().getInfo().title).append("**").append("\n");
                    toSend.append(String.format("%ss / %ss", nowSeconds, endSeconds));

                    toUpdate.editMessage(toSend.toString()).queue(this::setButtons);
                } catch (Exception e) {
                    toUpdate.editMessage("Воспроизведение завершено.").queue();
                    remove();
                    break;
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }

        public void remove() {
            toUpdate.delete().queue();
        }
    }
}
