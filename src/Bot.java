import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.events.DisconnectEvent;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.events.ReconnectedEvent;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Bot extends ListenerAdapter {
    private static final String BOT_TOKEN = "";
    private static final String Q_CHANNEL = "538166641226416162";
    private static String mode = "Random";
    private static Logger log;
    private List<String> queue = new ArrayList<>();

    public static void main(String args[]) throws LoginException
    {
        log = LoggerFactory.getLogger(Bot.class);
        JDABuilder builder = new JDABuilder(AccountType.BOT);
        builder.setToken(BOT_TOKEN)
                .addEventListener(new Bot())
                .build();
    }

    @Override
    public void onReady(ReadyEvent event)
    {
        log.info("Six Mans Bot Started.");
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String message = event.getMessage().getContentRaw();
        switch (message)
        {
            case"!q":
                if (event.getMessage().getChannel().getId().equals(Q_CHANNEL))
                    q(event.getAuthor().getId(), event);
                break;
            case "!leave":
                if (event.getMessage().getChannel().getId().equals(Q_CHANNEL))
                    leave(event.getAuthor().getId(), event);
                break;
            case "!status":
                if (event.getMessage().getChannel().getId().equals(Q_CHANNEL))
                    displayStatus(event);
                break;
            case "!cancel":
                if (event.getMessage().getChannel().getId().equals(Q_CHANNEL) && event.getMember().getRoles().stream().anyMatch(e -> e.getName().contentEquals("Bot Admin")))
                    cancel(event);
                break;
            case "!flip":
                coinFlip(event);
                break;
            case "!r":
                mode = "Random";
                break;
            case "!c":
                mode = "Captains";
                break;
        }
    }

    @Override
    public void onDisconnect(DisconnectEvent event) {
        log.info("Bot disconnected. Code: " + event.getCloseCode());
    }

    @Override
    public void onReconnect(ReconnectedEvent event) {
        log.info("Bot successfully reconnected.");
    }

    private void q(String id, MessageReceivedEvent event)
    {
        if (queue.contains(id))
        {
            return;
        }
        queue.add(id);
        EmbedBuilder result = new EmbedBuilder();
        result.setColor(Color.green);
        if (queue.size() == 6)
        {
            Collections.shuffle(queue);
            result.setTitle("Please join the first available lobby.");
            if (mode.equals("Captains")) {
                result.setDescription("You can use !flip to decide which captain gets first pick. Games are bo5. Good luck!");
                result.addField("Captain 1", "<@" + queue.get(0) + ">", true);
                result.addField("Captain 2", "<@" + queue.get(1) + ">", true);
            }
            else
            {
                result.setDescription("The teams have been randomized. Games are bo5. Good luck!");
                result.addField("Team 1", "<@" + queue.get(0) + ">" + " <@" + queue.get(1) + ">" + " <@" + queue.get(2) + ">", true);
                result.addField("Team 2", "<@" + queue.get(3) + ">" + " <@" + queue.get(4) + ">" + " <@" + queue.get(5) + ">", true);
            }
            StringBuilder sb = new StringBuilder();
            for (String s : queue) {
                sb.append("<@");
                sb.append(s);
                sb.append("> ");
            }
            queue.clear();
            event.getMessage().getChannel().sendMessage(sb).queue();
        }
        else
        {
            result.setTitle(queue.size() + " player(s) are in the queue");
            result.setDescription("<@" + id + "> has joined the queue!");
        }
        event.getMessage().getChannel().sendMessage(result.build()).queue();
    }

    private void leave(String id, MessageReceivedEvent event)
    {
        if (!queue.contains(id))
        {
            return;
        }
        queue.remove(id);
        EmbedBuilder result = new EmbedBuilder();
        result.setColor(Color.green);
        result.setTitle(queue.size() + " player(s) are in the queue");
        result.setDescription("<@" + id + "> has left the queue!");
        event.getMessage().getChannel().sendMessage(result.build()).queue();
    }

    private void cancel(MessageReceivedEvent event)
    {
        queue.clear();
        EmbedBuilder result = new EmbedBuilder();
        result.setColor(Color.green);
        result.setTitle(queue.size() + " player(s) are in the queue");
        result.setDescription("The queue is now empty.");
        event.getMessage().getChannel().sendMessage(result.build()).queue();
    }

    private void displayStatus(MessageReceivedEvent event)
    {
        EmbedBuilder result = new EmbedBuilder();
        result.setColor(Color.green);
        result.setTitle(queue.size() + " player(s) are in the queue");
        StringBuilder sb = new StringBuilder();
        for (String s : queue)
        {
            sb.append("<@");
            sb.append(s);
            sb.append("> ");
        }
        result.setDescription(sb);
        event.getMessage().getChannel().sendMessage(result.build()).queue();
    }

    private void coinFlip(MessageReceivedEvent event)
    {
        if (Math.random() < 0.5)
            event.getMessage().getChannel().sendMessage("Result: Heads").queue();
        else
            event.getMessage().getChannel().sendMessage("Result: Tails").queue();
    }
}