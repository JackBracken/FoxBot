package uk.co.revthefox.foxbot.commands;

import org.pircbotx.Channel;
import org.pircbotx.Colors;
import org.pircbotx.User;
import org.pircbotx.hooks.events.MessageEvent;
import uk.co.revthefox.foxbot.FoxBot;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class CommandBan extends Command
{
    private FoxBot foxbot;

    public CommandBan(FoxBot foxbot)
    {
        super("ban", "command.ban");
        this.foxbot = foxbot;
    }

    public void execute(final MessageEvent event, final String[] args)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                User sender = event.getUser();
                Channel channel = event.getChannel();

                if (args.length != 0)
                {
                    User target = foxbot.getUser(args[0]);
                    String hostmask = target.getHostmask();

                    if (!channel.getUsers().contains(target))
                    {
                        foxbot.sendNotice(sender, "That user is not in this channel!");
                        return;
                    }

                    // Please don't throttle me ;_;
                    try
                    {
                        Thread.sleep(foxbot.getConfig().getKickDelay());
                    }
                    catch (InterruptedException ex)
                    {
                        ex.printStackTrace();
                    }

                    if (foxbot.getPermissionManager().userHasQuietPermission(target, "protection.ban") || args[0].equals(foxbot.getNick()))
                    {
                        foxbot.sendNotice(sender, "You cannot ban that user!");
                        return;
                    }

                    if (args.length > 1)
                    {
                        final StringBuilder reason = new StringBuilder(args[1]);

                        for (int arg = 2; arg < args.length; arg++)
                        {
                            reason.append(" ").append(args[arg]);
                        }

                        foxbot.kick(channel, target, String.format("Ban requested by %s - %s", sender.getNick(), foxbot.getUtils().colourise(reason.toString()) + Colors.NORMAL));
                        foxbot.ban(channel, hostmask);

                        if (foxbot.getConfig().getUnbanTimer() > 0)
                        {
                            scheduleUnban(channel, hostmask);
                        }
                        return;
                    }
                    foxbot.kick(channel, target, String.format("Ban requested by %s", sender.getNick()));
                    foxbot.ban(channel, hostmask);

                    if (foxbot.getConfig().getUnbanTimer() > 0)
                    {
                        scheduleUnban(channel, hostmask);
                    }
                    return;
                }
                foxbot.sendNotice(sender, String.format("Wrong number of args! Use %sban <nick> [reason]", foxbot.getConfig().getCommandPrefix()));
            }
        }).start();
    }

    public void scheduleUnban(final Channel channel, final String hostmask)
    {
        new Timer().schedule(
                new TimerTask()
                {
                    @Override
                    public void run()
                    {
                        foxbot.unBan(channel, hostmask);
                    }
                },
                TimeUnit.SECONDS.toMillis(foxbot.getConfig().getUnbanTimer())
        );
    }
}
