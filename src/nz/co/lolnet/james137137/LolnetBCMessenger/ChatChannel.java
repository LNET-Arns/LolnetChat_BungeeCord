/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.LolnetBCMessenger;

import com.google.common.collect.Multimap;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.PluginManager;
import static nz.co.lolnet.james137137.LolnetBCMessenger.EventListener.isBroadCastMode;
import static nz.co.lolnet.james137137.LolnetBCMessenger.EventListener.isGlobalMode;
import static nz.co.lolnet.james137137.LolnetBCMessenger.EventListener.isNotifyMode;
import static nz.co.lolnet.james137137.LolnetBCMessenger.EventListener.isSilentMode;
import nz.co.lolnet.lolnetfourmpermissionbcbridge.LolnetFourmPermissionBCBridge;

/**
 *
 * @author James
 */
public class ChatChannel {

    private static HashMap<String, Boolean> SpyChat = new HashMap<>();
    public static HashMap<String, String> replyPerson = new HashMap<>();

    public static Logger log = Logger.getLogger(ChatChannel.class.getName());

    public static void toggleSpyChat(String playerName) {
        SpyChat.put(playerName, !SpyChatIsToggled(playerName));
    }

    public static void toggleSpyChat(String playerName, boolean vaule) {
        SpyChat.put(playerName, vaule);
    }

    static boolean SpyChatIsToggled(String playerName) {
        Boolean SpyMode = SpyChat.get(playerName);
        if (SpyMode == null) {
            SpyChat.put(playerName, true);
            return true;
        }
        return SpyMode;
    }

    public static class VIPChat extends Command {

        public VIPChat(LolnetBCMessenger aThis) {
            super("VIPChat", "LolnetBCMessenger.VIPChat", "gv");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {

            if (isSilentMode.contains(cs.getName())) {
                cs.sendMessage(ChatColor.RED + "You are in SilentMode, run /SilentMode or relog to turn off.");
                return;
            }
            if (strings.length == 0) {
                cs.sendMessage("§6/gv <messagehere>");
                return;
            }
            String message = "";
            for (String string : strings) {
                message += string + " ";
            }
            sendMessage(cs.getName(), message, "VIP");

        }

        private void sendMessage(String senderName, String message, String channelName) {
            log.info("[" + channelName + "Channel]:" + senderName + ":" + message);
            String serverName = LolnetBCMessenger.plugin.getProxy().getPlayer(senderName).getServer().getInfo().getName();
            String messageToSend = ChatColor.GOLD + "[" + "VIP" + "-Chat] " + "§6[" + serverName + "]: §r" + senderName + ": " + message;
            try {
                com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI api = com.imaginarycode.minecraft.redisbungee.RedisBungee.getApi();

                api.sendChannelMessage("LolnetBCMessenger", "player:ALLTHEPLAYERS" + "######" + "LolnetBCMessenger.VIPChat" + "######" + messageToSend);

            } catch (Exception e) {
                for (ProxiedPlayer proxiedPlayer : LolnetBCMessenger.plugin.getProxy().getPlayers()) {
                    if (proxiedPlayer.hasPermission("LolnetBCMessenger.VIPChat")) {
                        proxiedPlayer.sendMessage(messageToSend);
                    }

                }
            }

        }
    }

    public static class AdminChat extends Command {

        public AdminChat(LolnetBCMessenger aThis) {
            super("adminChat", "LolnetBCMessenger.AdminChat", "ac");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {

            if (isSilentMode.contains(cs.getName())) {
                cs.sendMessage(ChatColor.RED + "You are in SilentMode, run /SilentMode or relog to turn off.");
                return;
            }
            if (strings.length == 0) {
                cs.sendMessage("§6/ac <messagehere>");
                return;
            }
            String message = "";
            for (String string : strings) {
                message += string + " ";
            }
            sendMessage(cs.getName(), message, "ADMIN");

        }

        private void sendMessage(String senderName, String message, String channelName) {
            log.info("[" + channelName + "Channel]:" + senderName + ":" + message);
            String serverName = LolnetBCMessenger.plugin.getProxy().getPlayer(senderName).getServer().getInfo().getName();

            String messageToSend = "§c[" + "Admin" + "-Chat] " + "§6[" + serverName + "]: §r" + senderName + ": " + message;
            try {
                com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI api = com.imaginarycode.minecraft.redisbungee.RedisBungee.getApi();

                api.sendChannelMessage("LolnetBCMessenger", "player:ALLTHEPLAYERS" + "######" + "LolnetBCMessenger.AdminChat" + "######" + messageToSend);

            } catch (Exception e) {
                for (ProxiedPlayer proxiedPlayer : LolnetBCMessenger.plugin.getProxy().getPlayers()) {
                    if (proxiedPlayer.hasPermission("LolnetBCMessenger.AdminChat")) {
                        proxiedPlayer.sendMessage(messageToSend);
                    }

                }
            }

        }
    }

    public static class ModChat extends Command {

        public ModChat(LolnetBCMessenger aThis) {
            super("ModChat", "LolnetBCMessenger.ModChat", "mc", "mb", "sc");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {

            if (isSilentMode.contains(cs.getName())) {
                cs.sendMessage(ChatColor.RED + "You are in SilentMode, run /SilentMode or relog to turn off.");
                return;
            }
            if (strings.length == 0) {
                cs.sendMessage("§6/mb <messagehere>");
                return;
            }
            String message = "";
            for (String string : strings) {
                message += string + " ";
            }
            sendMessage(cs.getName(), message, "Mod");

        }

        private void sendMessage(String senderName, String message, String channelName) {
            log.info("[" + channelName + "Channel]:" + senderName + ":" + message);
            String serverName = LolnetBCMessenger.plugin.getProxy().getPlayer(senderName).getServer().getInfo().getName();
            String messageToSend = "§3[" + "Mod" + "-Chat] " + "§6[" + serverName + "]: §r" + senderName + ": " + message;
            try {
                com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI api = com.imaginarycode.minecraft.redisbungee.RedisBungee.getApi();

                api.sendChannelMessage("LolnetBCMessenger", "player:ALLTHEPLAYERS" + "######" + "LolnetBCMessenger.ModChat" + "######" + messageToSend);

            } catch (Exception e) {
                for (ProxiedPlayer proxiedPlayer : LolnetBCMessenger.plugin.getProxy().getPlayers()) {
                    if (proxiedPlayer.hasPermission("LolnetBCMessenger.ModChat")) {
                        proxiedPlayer.sendMessage(messageToSend);
                    }

                }
            }

        }
    }

    public static class UAChat extends Command {

        public UAChat(LolnetBCMessenger aThis) {
            super("UAChat", "LolnetBCMessenger.UAChat", "uc");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {

            if (isSilentMode.contains(cs.getName())) {
                cs.sendMessage(ChatColor.RED + "You are in SilentMode, run /SilentMode or relog to turn off.");
                return;
            }
            if (strings.length == 0) {
                cs.sendMessage("§6/uc <messagehere>");
                return;
            }
            String message = "";
            for (String string : strings) {
                message += string + " ";
            }
            sendMessage(cs.getName(), message, "UA");

        }

        private void sendMessage(String senderName, String message, String channelName) {
            log.info("[" + channelName + "Channel]:" + senderName + ":" + message);
            String serverName = LolnetBCMessenger.plugin.getProxy().getPlayer(senderName).getServer().getInfo().getName();
            String messageToSend = "§2[" + "UA" + "-Chat] " + "§6[" + serverName + "]: §r" + senderName + ": " + message;
            try {
                com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI api = com.imaginarycode.minecraft.redisbungee.RedisBungee.getApi();

                api.sendChannelMessage("LolnetBCMessenger", "player:ALLTHEPLAYERS" + "######" + "LolnetBCMessenger.UAChat" + "######" + messageToSend);

            } catch (Exception e) {
                for (ProxiedPlayer proxiedPlayer : LolnetBCMessenger.plugin.getProxy().getPlayers()) {
                    if (proxiedPlayer.hasPermission("LolnetBCMessenger.UAChat")) {
                        proxiedPlayer.sendMessage(messageToSend);
                    }

                }
            }

        }
    }

    public static class debugTest extends Command {

        public debugTest(LolnetBCMessenger aThis) {
            super("debugTest");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {
            if (true) {
                return;
            }
            PluginManager pluginManager = LolnetBCMessenger.plugin.getProxy().getPluginManager();
            Field[] fields = LolnetBCMessenger.plugin.getProxy().getClass().getFields();
            cs.sendMessage("" + fields.length);
            for (Field field : fields) {
                cs.sendMessage(field.getName());
            }
        }
    }

    public static class PrivateChat extends Command {

        public PrivateChat(LolnetBCMessenger aThis) {
            super("message", null, "tell", "m", "msg", "w", "whisper");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {
            if (isSilentMode.contains(cs.getName())) {
                cs.sendMessage(ChatColor.RED + "You are in SilentMode, run /SilentMode or relog to turn off.");
                return;
            }

            if (strings.length <= 1) {
                cs.sendMessage("§6/msg playerName messagehere");
                return;
            }
            com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI api = com.imaginarycode.minecraft.redisbungee.RedisBungee.getApi();
            String playerNameT = strings[0];
            String correctPlayerName = "NULLPLAYER";
            for (String playerS : api.getHumanPlayersOnline()) {
                if (playerNameT.equalsIgnoreCase(playerS)) {
                    correctPlayerName = playerS;
                    break;
                }
            }
            if (correctPlayerName.equals("NULLPLAYER")) {
                for (String playerS : api.getHumanPlayersOnline()) {
                    if (playerS.toLowerCase().contains(playerNameT.toLowerCase())) {
                        correctPlayerName = playerS;
                        break;
                    }
                }
            }
            if (correctPlayerName.equals("NULLPLAYER")) {
                cs.sendMessage(ChatColor.RED + "Player not found, check if the player is online with /glist");
                return;
            }
            if (isSilentMode.contains(correctPlayerName)) {
                cs.sendMessage("It seems that player is in silentMode");
                return;
            }
            String message = "";
            for (int i = 1; i < strings.length; i++) {
                message += strings[i] + " ";
            }
            String correctPlayerPrefix = "";
            String senderPrefix = "";
            try {
                correctPlayerPrefix = ChatChannel.getprefix(LolnetFourmPermissionBCBridge.getHighestRank(correctPlayerName));

            } catch (Exception ex) {
            }
            try {
                senderPrefix = ChatChannel.getprefix(LolnetFourmPermissionBCBridge.getHighestRank(cs.getName()));
            } catch (Exception e) {
            }

            String messageToSend1 = ChatColor.GOLD + "[me -> " + ChatColor.RESET
                    + correctPlayerPrefix + correctPlayerName + ChatColor.GOLD + "] " + ChatColor.RESET + message;
            String messageToSend2 = ChatColor.GOLD + "" + ChatColor.GOLD + "[" + ChatColor.RESET
                    + senderPrefix + cs.getName() + ChatColor.GOLD + " -> me] " + ChatColor.RESET + message;
            String SpyChat = ChatColor.GRAY + "[" + cs.getName() + " -> " + correctPlayerName + ChatColor.GRAY + "] " + ChatColor.RESET + message;

            cs.sendMessage(messageToSend1);
            
            api.sendChannelMessage("LolnetBCMessenger", "replyLink:" + correctPlayerName + ":" + cs.getName() + "######" + "NULL" + "######" + "NULL");
            api.sendChannelMessage("LolnetBCMessenger", "player:" + correctPlayerName + "######" + "NULL" + "######" + messageToSend2);
            if (isPVPServerRelated(correctPlayerName, cs.getName())) {
                api.sendChannelMessage("LolnetBCMessenger", "SpyChat" + "######" + "LolnetBCMessenger.spyChatPVPServer" + "######" + SpyChat);
            } else {
                api.sendChannelMessage("LolnetBCMessenger", "SpyChat" + "######" + "LolnetBCMessenger.spyChat" + "######" + SpyChat);
            }

        }

    }

    private static boolean isPVPServerRelated(String player1, String player2) {
        com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI api = com.imaginarycode.minecraft.redisbungee.RedisBungee.getApi();
        List<String> PVPServers = LolnetBCMessenger.PVPServers;
        UUID player1UUID = api.getUuidFromName(player1);
        UUID player2UUID = api.getUuidFromName(player2);
        for (String PVPServer : PVPServers) {
            if (api.getPlayersOnServer(PVPServer).contains(player1UUID) || api.getPlayersOnServer(PVPServer).contains(player2UUID))
            {
                return true;
            }
        }
        return false;
    }

    static class SpyChatCommand extends Command {

        public SpyChatCommand(LolnetBCMessenger aThis) {
            super("spychat", "LolnetBCMessenger.spyChat", "socialspy");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {
            ChatChannel.toggleSpyChat(cs.getName());
            if (ChatChannel.SpyChatIsToggled(cs.getName())) {
                cs.sendMessage(ChatColor.GREEN + "Spy mode is now on");
            } else {
                cs.sendMessage(ChatColor.GREEN + "Spy mode is now off");
            }
        }
    }

    public static class ReplyChat extends Command {

        public ReplyChat(LolnetBCMessenger aThis) {
            super("reply", null, "r");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {
            if (isSilentMode.contains(cs.getName())) {
                cs.sendMessage(ChatColor.RED + "You are in SilentMode, run /SilentMode or relog to turn off.");
                return;
            }
            String correctPlayerName = ChatChannel.replyPerson.get(cs.getName());
            if (isSilentMode.contains(correctPlayerName)) {
                cs.sendMessage("It seems that player is in silentMode");
                return;
            }
            com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI api = com.imaginarycode.minecraft.redisbungee.RedisBungee.getApi();
            if (correctPlayerName == null || !api.getHumanPlayersOnline().contains(correctPlayerName)) {
                cs.sendMessage(ChatColor.RED + "Player not found");
                return;
            }
            if (strings.length < 1) {
                cs.sendMessage("§6/r messagehere");
                return;
            }
            String message = "";
            for (int i = 0; i < strings.length; i++) {
                message += strings[i] + " ";
            }
            String correctPlayerPrefix = "";
            String senderPrefix = "";
            try {
                correctPlayerPrefix = ChatChannel.getprefix(LolnetFourmPermissionBCBridge.getHighestRank(correctPlayerName));

            } catch (Exception ex) {
            }
            try {
                senderPrefix = ChatChannel.getprefix(LolnetFourmPermissionBCBridge.getHighestRank(cs.getName()));
            } catch (Exception e) {
            }

            String messageToSend1 = ChatColor.GOLD + "[me -> " + ChatColor.RESET
                    + correctPlayerPrefix + correctPlayerName + ChatColor.GOLD + "] " + ChatColor.RESET + message;
            String messageToSend2 = ChatColor.GOLD + "" + ChatColor.GOLD + "[" + ChatColor.RESET
                    + senderPrefix + cs.getName() + ChatColor.GOLD + " -> me] " + ChatColor.RESET + message;
            String SpyChat = ChatColor.GRAY + "[" + cs.getName() + " -> " + correctPlayerName + ChatColor.GRAY + "] " + ChatColor.RESET + message;

            cs.sendMessage(messageToSend1);
            api.sendChannelMessage("LolnetBCMessenger", "replyLink:" + correctPlayerName + ":" + cs.getName() + "######" + "NULL" + "######" + "NULL");
            api.sendChannelMessage("LolnetBCMessenger", "player:" + correctPlayerName + "######" + "NULL" + "######" + messageToSend2);
            if (isPVPServerRelated(correctPlayerName, cs.getName())) {
                api.sendChannelMessage("LolnetBCMessenger", "SpyChat" + "######" + "LolnetBCMessenger.spyChatPVPServer" + "######" + SpyChat);
            } else {
                api.sendChannelMessage("LolnetBCMessenger", "SpyChat" + "######" + "LolnetBCMessenger.spyChat" + "######" + SpyChat);
            }

        }
    }

    public static String getprefix(Collection<String> groups) {

        Object[] toArray = groups.toArray();
        String group = "default";
        for (Object object : toArray) {
            if (!("" + object).equalsIgnoreCase("default")) {
                group = "" + object;
            }
        }
        String string1 = LolnetBCMessenger.plugin.getConfig().getString(group);
        if (string1 != null) {
            return string1.replaceAll("&", "§");
        }
        return "";
    }

    public static String getprefix(String Highestgroup) {
        String string1 = LolnetBCMessenger.plugin.getConfig().getString(Highestgroup);
        if (string1 != null) {
            return string1.replaceAll("&", "§");
        }
        return "";
    }

    public static class reloadCommand extends Command {

        public reloadCommand(LolnetBCMessenger aThis) {
            super("LolnetBCMessengerReload", "LolnetBCMessenger.Reload", "LBCMReload");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {
            LolnetBCMessenger.plugin.reload();
            SpyChat = new HashMap<>();
            cs.sendMessage(ChatColor.GREEN + "Reloaded!");
        }
    }

    public static class SlientMode extends Command {

        public SlientMode(LolnetBCMessenger aThis) {
            super("SilentMode", "LolnetBCMessenger.ModChat");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {
            if (isSilentMode.contains(cs.getName())) {
                isSilentMode.remove(cs.getName());
                cs.sendMessage(ChatColor.GREEN + "SilentMode is now: " + ChatColor.GOLD + "off");
                if (cs.hasPermission("LolnetBCMessenger.spyChat")) {
                    if (!ChatChannel.SpyChatIsToggled(cs.getName())) {
                        toggleSpyChat(cs.getName(), true);
                        cs.sendMessage(ChatColor.GREEN + "Spy mode is now " + ChatColor.GOLD + "on");
                    }

                }

            } else {
                isSilentMode.add(cs.getName());
                cs.sendMessage(ChatColor.GREEN + "SilentMode is now: " + ChatColor.GOLD + "on");
                if (cs.hasPermission("LolnetBCMessenger.spyChat")) {

                    if (ChatChannel.SpyChatIsToggled(cs.getName())) {
                        toggleSpyChat(cs.getName(), false);
                        cs.sendMessage(ChatColor.GREEN + "Spy mode is now " + ChatColor.GOLD + "off");
                    }

                }
            }
        }
    }

    public static class toggleGlobalChat extends Command {

        public toggleGlobalChat(LolnetBCMessenger aThis) {
            super("toggleglobalchat", "LolnetBCMessenger.toggleglobalchat", "tgc");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {
            if (isGlobalMode.contains(cs.getName())) {
                isGlobalMode.remove(cs.getName());
                cs.sendMessage(ChatColor.GREEN + "GlobalMode is now: " + ChatColor.GOLD + "on " + ChatColor.GREEN + "- You are able to see chat from other servers.");
            } else {
                isGlobalMode.add(cs.getName());
                cs.sendMessage(ChatColor.GREEN + "GlobalMode is now: " + ChatColor.GOLD + "off " + ChatColor.GREEN + "- You will be unable to see chat from other servers.");
            }
        }
    }

    public static class toggleBroadcastChat extends Command {

        public toggleBroadcastChat(LolnetBCMessenger aThis) {
            super("togglebroadcastchat", "LolnetBCMessenger.togglebroadcastchat", "tbc");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {
            if (isBroadCastMode.contains(cs.getName())) {
                isBroadCastMode.remove(cs.getName());
                cs.sendMessage(ChatColor.GREEN + "BroadCastMode is now: " + ChatColor.GOLD + "off");
            } else {
                isBroadCastMode.add(cs.getName());
                cs.sendMessage(ChatColor.GREEN + "BroadCastMode is now: " + ChatColor.GOLD + "on");
            }
        }
    }

    public static class toggleFilterNotify extends Command {

        public toggleFilterNotify(LolnetBCMessenger aThis) {
            super("toggleFilterNotify", "LolnetBCMessenger.filter.notify", "tfn");
        }

        @Override
        public void execute(CommandSender cs, String[] strings) {
            if (isNotifyMode.contains(cs.getName())) {
                isNotifyMode.remove(cs.getName());
                cs.sendMessage(ChatColor.GREEN + "Filter Notify is now: " + ChatColor.GOLD + "on");
            } else {
                isNotifyMode.add(cs.getName());
                cs.sendMessage(ChatColor.GREEN + "Filter Notify is now: " + ChatColor.GOLD + "off");
            }
        }
    }

}
