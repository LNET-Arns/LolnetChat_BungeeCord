/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.LolnetBCMessenger;

import fr.Alphart.BAT.BAT;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Level;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import static nz.co.lolnet.james137137.LolnetBCMessenger.ChatChannel.replyPerson;

/**
 *
 * @author James
 */
public class EventListener implements Listener {

    public static LolnetBCMessenger plugin;
    public static HashSet<String> isSilentMode;
    public static HashSet<String> isGlobalMode;
    public static HashSet<String> isBroadCastMode;
    public static HashSet<String> isNotifyMode;
    HashMap<String, Long> lastChatTime;
    HashMap<String, String> lastMessage;
    HashMap<String, Integer> warningCount;
    final int maxWarning = 5;

    public EventListener(LolnetBCMessenger aThis) {
        plugin = aThis;
        isSilentMode = new HashSet<>();
        isGlobalMode = new HashSet<>();
        isBroadCastMode = new HashSet<>();
        isNotifyMode = new HashSet<>();
        lastChatTime = new HashMap<>();
        lastMessage = new HashMap<>();
        warningCount = new HashMap<>();
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent event) {
        warningCount.put(event.getPlayer().getName(), 0);
    }

    @EventHandler
    public void onPlayerQuit(PlayerDisconnectEvent event) {
        ChatChannel.replyPerson.remove(event.getPlayer().getName());
        lastChatTime.remove(event.getPlayer().getName());
        lastMessage.remove(event.getPlayer().getName());
        warningCount.remove(event.getPlayer().getName());
        isSilentMode.remove(event.getPlayer().getName());
        isGlobalMode.remove(event.getPlayer().getName());
        isBroadCastMode.remove(event.getPlayer().getName());
    }

    @EventHandler
    public void onTab(TabCompleteEvent event) {
        if (!event.getSuggestions().isEmpty()) {
            return; //If suggestions for this command are handled by other plugin don't add any
        }

        String[] args = event.getCursor().split(" ");

        com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI api = com.imaginarycode.minecraft.redisbungee.RedisBungee.getApi();

        final String checked = (args.length > 0 ? args[args.length - 1] : event.getCursor()).toLowerCase();
        for (String player : api.getHumanPlayersOnline()) {
            if (player.toLowerCase().startsWith(checked)) {
                event.getSuggestions().add(player);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(ChatEvent event) {
        if (event.isCommand() || event.isCancelled()) {
            return;
        }

        if (event.getSender() instanceof ProxiedPlayer) {
            if (event.getMessage().startsWith("/")) {
                return;
            }
            ProxiedPlayer player = (ProxiedPlayer) event.getSender();
            boolean isSpamming = ApplySpamFilter(event, player);
            if (isSpamming) {
                event.setCancelled(true);
                return;
            }

            String ServerName = player.getServer().getInfo().getName();
            if (isMuted(player, ServerName)) {
                return;
            }

            if (ServerName.equalsIgnoreCase("factions")) {
                return;
            }
            applyFilter(event, player);
            ThreadSendToOtherServers threadSendToOtherServers = new ThreadSendToOtherServers(player, event.getMessage());

        }
    }

    @EventHandler
    public void onPubSubMessageEvent(com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent event) {
        if (!event.getChannel().equals("LolnetBCMessenger")) {
            return;
        }

        String message = event.getMessage();
        String[] split = message.split("######");
        String target = split[0];
        String permission = split[1];
        String messageToSend = split[2];
        if (split.length >= 3) {
            for (int i = 3; i < split.length; i++) {
                messageToSend += split[i];

            }
        }
        TextComponent tc = null;
        if (target.startsWith("TextComponent:")) {
            target = target.replaceFirst("TextComponent:", "");
            String[] split1 = messageToSend.split("###");
            String serverShort = split1[0];
            String serverName = split1[1];
            messageToSend = split1[2];
            for (int i = 3; i < split1.length; i++) {
                messageToSend += split1[i];
            }
            tc = new TextComponent("");
            TextComponent serverNameClickable = new TextComponent(serverShort);
            serverNameClickable.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + serverName));
            serverNameClickable.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GOLD + serverName).create()));
            tc.addExtra(serverNameClickable);
            tc.addExtra(new TextComponent(messageToSend));

        }

        if (!target.startsWith("SpyChat")) {
            LolnetBCMessenger.log(target + "|" + messageToSend);
        }

        if (target.startsWith("player:")) {
            String playerName = target.split(":")[1];
            if (playerName.equals("ALLTHEPLAYERS")) {
                for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                    if (permission.equals("NULL") || player.hasPermission(permission)) {
                        if (!isSilentMode.contains(player.getName())) {
                            if (tc == null) {
                                player.sendMessage(messageToSend);
                            } else {
                                player.sendMessage(tc);
                            }

                        }

                    }
                }
            } else {
                ProxiedPlayer player = BungeeCord.getInstance().getPlayer(playerName);
                if (player != null && (permission.equals("NULL") || player.hasPermission(permission))) {
                    if (!isSilentMode.contains(player.getName())) {
                        if (tc == null) {
                            player.sendMessage(messageToSend);
                        } else {
                            player.sendMessage(tc);
                        }
                    }

                }
            }

        } else if (target.startsWith("ALLSERVER")) {
            HashSet<String> serversToExclude = new HashSet<>();
            if (target.contains("!")) {
                String[] split1 = target.split("!");

                for (int i = 0; i < split1.length; i++) {
                    serversToExclude.add(split1[i]);
                }
            }
            for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {

                if (!isGlobalMode.contains(player.getName())) {
                    try {
                        if (!serversToExclude.contains(player.getServer().getInfo().getName())) {
                            if (permission.equals("NULL") || player.hasPermission(permission)) {
                                if (tc == null) {
                                    player.sendMessage(messageToSend);
                                } else {
                                    player.sendMessage(tc);
                                }

                            }
                        }
                    } catch (Exception e) {
                    }
                }

            }
        } else if (target.startsWith("SERVER:")) {
            String serverName = target.split(":")[1];
            ServerInfo server = BungeeCord.getInstance().getServers().get(serverName);
            if (server != null) {
                Collection<ProxiedPlayer> players = server.getPlayers();
                for (ProxiedPlayer player : players) {
                    if (!isGlobalMode.contains(player.getName())) {
                        if (tc == null) {
                            player.sendMessage(messageToSend);
                        } else {
                            player.sendMessage(tc);
                        }
                    }

                }
            }

        } else if (target.startsWith("NOBYPASS_player:")) {
            String playerName = target.split(":")[1];
            if (playerName.equals("ALLTHEPLAYERS")) {
                for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                    if (permission.equals("NULL") || player.hasPermission(permission)) {
                        boolean sendMessage = true;
                        if (permission.equals("LolnetBCMessenger.filter.notify")) {
                            if (isNotifyMode.contains(player.getName())) {
                                sendMessage = false;
                            }
                        }
                        if (sendMessage) {
                            if (tc == null) {
                                player.sendMessage(messageToSend);
                            } else {
                                player.sendMessage(tc);
                            }
                        }

                    }
                }
            } else {
                ProxiedPlayer player = BungeeCord.getInstance().getPlayer(playerName);
                if (player != null && (permission.equals("NULL") || player.hasPermission(permission))) {
                    if (tc == null) {
                        player.sendMessage(messageToSend);
                    } else {
                        player.sendMessage(tc);
                    }

                }
            }

        } else if (target.startsWith("NOBYPASS_ALLSERVER")) {
            HashSet<String> serversToExclude = new HashSet<>();
            if (target.contains("!")) {
                String[] split1 = target.split("!");

                for (int i = 0; i < split1.length; i++) {
                    serversToExclude.add(split1[i]);
                }
            }
            for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                System.out.println(player.getName());

                try {
                    if (!serversToExclude.contains(player.getServer().getInfo().getName())) {
                        if (permission.equals("NULL") || player.hasPermission(permission)) {
                            if (tc == null) {
                                player.sendMessage(messageToSend);
                            } else {
                                player.sendMessage(tc);
                            }

                        }
                    }
                } catch (Exception e) {
                }

            }
        } else if (target.startsWith("NOBYPASS_SERVER:")) {
            String serverName = target.split(":")[1];
            ServerInfo server = BungeeCord.getInstance().getServers().get(serverName);
            if (server != null) {
                Collection<ProxiedPlayer> players = server.getPlayers();
                for (ProxiedPlayer player : players) {

                    if (tc == null) {
                        player.sendMessage(messageToSend);
                    } else {
                        player.sendMessage(tc);
                    }

                }
            }

        } else if (target.startsWith("SpyChat")) {
            String filterBit = messageToSend.split(ChatColor.GOLD + "] ")[0];
            for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
                if ((permission.equals("NULL") || player.hasPermission(permission)) && ChatChannel.SpyChatIsToggled(player.getName())) {
                    if (!filterBit.contains(player.getName())) {
                        if (tc == null) {
                            player.sendMessage(messageToSend);
                        } else {
                            player.sendMessage(tc);
                        }

                    }

                }

            }
        } else if (target.startsWith("replyLink:")) {
            String player1Name = target.split(":")[1];
            String player2Name = target.split(":")[2];
            ProxiedPlayer player1 = BungeeCord.getInstance().getPlayer(player1Name);
            if (player1 != null) {
                replyPerson.put(player1Name, player2Name);
            }
            ProxiedPlayer player2 = BungeeCord.getInstance().getPlayer(player2Name);
            if (player2 != null) {
                replyPerson.put(player2Name, player1Name);
            }
        }
    }

    private boolean isMuted(ProxiedPlayer player, String ServerName) {
        try {
            return BAT.getInstance().getModules().getMuteModule().isMute(player, ServerName) > 0;
        } catch (Exception ex) {
            return false;
        }
    }

    private void applyFilter(ChatEvent event, ProxiedPlayer player) {
        if (player.hasPermission("LolnetBCMessenger.filter.bypass")) {
            return;
        }
        String oldMessage = event.getMessage();
        String message = event.getMessage();
        double numberOfUpperCase = 0;
        double numberOfUpperCaseWords = 0;
        String[] words = message.split(" ");
        for (String word : words) {
            if (!isUsername(word)) {
                double numberOfUpperCaseOnWord = 0;
                for (int i = 0; i < word.length(); i++) {
                    if (Character.isUpperCase(word.charAt(i))) {
                        numberOfUpperCase++;
                        numberOfUpperCaseOnWord++;
                    }

                }
                if (numberOfUpperCaseOnWord <= word.length()) {
                    numberOfUpperCaseWords++;
                }
            }

        }
        if (numberOfUpperCase >= 4 && numberOfUpperCase / ((double) message.length()) >= 0.3 && message.length() >= 5 && numberOfUpperCaseWords / ((double) words.length) >= 0.4) {

            String output;
            String[] split = message.split(" ");

            split[0] = split[0].substring(0, 1).toUpperCase() + split[0].substring(1).toLowerCase();
            output = split[0];
            if (split.length >= 2) {
                for (int i = 1; i < split.length; i++) {
                    String string = split[i];
                    if (!isUsername(string)) {
                        string = string.toLowerCase();
                    }
                    output += " " + string;
                }
            }
            player.sendMessage(ChatColor.RED + "Please don't use too many capital letters.");
            event.setMessage(output);

            String messageToSend = ChatColor.RED + player.getName() + " was caught by the chat filter for: HIGHCAPS";
            String messageToSend2 = ChatColor.RED + "Message was the following: " + oldMessage;
            /*try {
            com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI api = com.imaginarycode.minecraft.redisbungee.RedisBungee.getApi();
            
            api.sendChannelMessage("LolnetBCMessenger", "NOBYPASS_player:ALLTHEPLAYERS" + "######" + "LolnetBCMessenger.filter.notify" + "######" + messageToSend);
            api.sendChannelMessage("LolnetBCMessenger", "NOBYPASS_player:ALLTHEPLAYERS" + "######" + "LolnetBCMessenger.filter.notify" + "######" + messageToSend2);
            
            } catch (Exception e) {
            for (ProxiedPlayer proxiedPlayer : LolnetBCMessenger.plugin.getProxy().getPlayers()) {
            if (proxiedPlayer.hasPermission("LolnetBCMessenger.filter.notify")) {
            proxiedPlayer.sendMessage(messageToSend);
            proxiedPlayer.sendMessage(ChatColor.RED + "Message was the following: " + oldMessage);
            }
            
            }
            }*/
            LolnetBCMessenger.log(messageToSend);
            LolnetBCMessenger.log(messageToSend2);
        }
    }

    private boolean isUsername(String word) {
        boolean isUsername;

        word = word.replaceAll("[^A-Za-z0-9-_]", "");

        try {
            isUsername = com.imaginarycode.minecraft.redisbungee.RedisBungee.getApi().getHumanPlayersOnline().contains(word);
        } catch (Exception e) {
            isUsername = plugin.getProxy().getPlayer(word) != null;
        }
        return isUsername;
    }

    private boolean ApplySpamFilter(ChatEvent event, ProxiedPlayer player) {
        boolean isSpamming = false;

        Long lastchat = lastChatTime.get(player.getName());
        if (!player.hasPermission("LolnetBCMessenger.filter.bypass")) {
            //if (true) {
            if (lastchat != null) {
                if (System.currentTimeMillis() - lastchat <= 250) {
                    isSpamming = true;
                } else if (System.currentTimeMillis() - lastchat <= 1000) {
                    String lastmessage = lastMessage.get(player.getName());
                    if (lastmessage != null && lastmessage.equals(event.getMessage())) {
                        isSpamming = true;
                    }
                }
            }

        }

        lastChatTime.put(player.getName(), System.currentTimeMillis());
        lastMessage.put(player.getName(), event.getMessage());

        Integer warnCount = warningCount.get(player.getName());
        if (warnCount == null) {
            warnCount = 0;
        }
        if (isSpamming) {
            warnCount++;
            warningCount.put(player.getName(), warnCount);
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Please wait before sending another message");
            if (warnCount >= 5) {
                player.disconnect("Spamming");

                String messageToSend = ChatColor.RED + player.getName() + " was caught and kicked by the chat filter for: Spamming";
                try {
                    com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI api = com.imaginarycode.minecraft.redisbungee.RedisBungee.getApi();

                    api.sendChannelMessage("LolnetBCMessenger", "NOBYPASS_player:ALLTHEPLAYERS" + "######" + "LolnetBCMessenger.filter.notify" + "######" + messageToSend);
                    api.sendChannelMessage("LolnetBCMessenger", "NOBYPASS_player:ALLTHEPLAYERS" + "######" + "LolnetBCMessenger.filter.notify" + "######" + ChatColor.RED + "Message was the following: " + event.getMessage());

                } catch (Exception e) {
                    for (ProxiedPlayer proxiedPlayer : LolnetBCMessenger.plugin.getProxy().getPlayers()) {
                        if (proxiedPlayer.hasPermission("LolnetBCMessenger.filter.notify")) {
                            proxiedPlayer.sendMessage(messageToSend);
                            proxiedPlayer.sendMessage(ChatColor.RED + "Message was the following: " + event.getMessage());
                        }

                    }
                }
            }

        } else {
            if (warnCount > 0) {
                warningCount.put(player.getName(), warnCount--);
            }

        }

        return isSpamming;
    }

    private static class ThreadSendToOtherServers {

        ProxiedPlayer player;
        String message;

        public ThreadSendToOtherServers(ProxiedPlayer player, String message) {
            this.player = player;
            this.message = message;
            start();
        }

        private void start() {
            plugin.getProxy().getScheduler().runAsync(plugin, new Runnable() {
                @Override
                public void run() {
                    String formatedMesssage = formatMessage(player, message, player.hasPermission("LolnetBCMessenger.ColourChat"));

                    try {
                        com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI api = com.imaginarycode.minecraft.redisbungee.RedisBungee.getApi();
                        if (isBroadCastMode.contains(player.getName())) {
                            api.sendChannelMessage("LolnetBCMessenger", "TextComponent:NOBYPASS_ALLSERVER!" + player.getServer().getInfo().getName() + "######" + "NULL" + "######" + formatedMesssage);
                        } else {
                            api.sendChannelMessage("LolnetBCMessenger", "TextComponent:ALLSERVER!" + player.getServer().getInfo().getName() + "######" + "NULL" + "######" + formatedMesssage);
                        }

                    } catch (Exception e) {
                        for (ProxiedPlayer proxiedPlayer : LolnetBCMessenger.plugin.getProxy().getPlayers()) {
                            proxiedPlayer.sendMessage(formatedMesssage);

                        }
                    }

                }
            });
        }

    }

    private static String formatMessage(ProxiedPlayer player, String message, boolean isMod) {
        String serverName = LolnetBCMessenger.plugin.getProxy().getPlayer(player.getName()).getServer().getInfo().getName();
        if (isMod) {
            message = message.replaceAll("&", "§");
        }
        String output = "";
        String sep = "###";
        String serverShort = plugin.getConfig().getString("ServerShorten." + serverName);
        if (serverShort == null || serverShort.length() <= 0) {
            serverShort = "[" + ChatColor.GOLD + serverName + ChatColor.RESET + "]";
        }
        serverShort = serverShort.replaceAll("&", "§");
        output += serverShort + sep;
        output += serverName + sep;

        String playerName = ChatChannel.getprefix(player.getGroups()) + player.getDisplayName() + ChatColor.RESET;
        output += " " + playerName + ": " + message;
        plugin.getProxy().getLogger().log(Level.INFO, "[{0}{1}{2}" + "]" + " [{3}] {4}", new Object[]{ChatColor.YELLOW, serverName, ChatColor.RESET, player.getDisplayName(), message});
        return output;
    }

    private static String formatMessage2(ProxiedPlayer player, String message, boolean isMod) {
        if (isMod) {
            message = message.replaceAll("&", "§");
        }
        return message;
    }

}
