/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.lolnetchatbc;

import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import com.google.common.io.ByteStreams;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

/**
 *
 * @author James
 */
public class LolnetChatBC extends Plugin {

    public static LolnetChatBC plugin;
    private static File myLogFile;
    static List<String> PVPServers;
    Configuration config;

    public static void main(String[] args) {
        System.out.println("HELLO_#!@#!".replaceAll("[^A-Za-z0-9-_]", ""));
    }

    protected static void log(String string) {

        String output;
        java.util.Date date = new java.util.Date();
        String time = new Timestamp(date.getTime()).toString();
        output = time + ":" + string;
        try {
            FileWriter fw = new FileWriter(myLogFile, true); //the true will append the new data
            fw.write(output + "\n");//appends the string to the file
            fw.close();
        } catch (IOException ioe) {
            System.err.println("IOException: " + ioe.getMessage());
        }
    }

    @Override
    public void onEnable() {
        setupLogger();
        setupConfig();
        config = getConfig();
        saveConfig();
        config = getConfig();
        plugin = this;
        PVPServers = config.getStringList("PVPServers");
        RedisBungee.getApi().registerPubSubChannels("LolnetBCMessenger");
        getProxy().getPluginManager().registerCommand(this, new ChatChannel.reloadCommand(this));
        getProxy().getPluginManager().registerCommand(this, new ChatChannel.PrivateChat(this));
        getProxy().getPluginManager().registerCommand(this, new ChatChannel.ReplyChat(this));
        getProxy().getPluginManager().registerCommand(this, new ChatChannel.SpyChatCommand(this));
        getProxy().getPluginManager().registerCommand(this, new ChatChannel.AdminChat(this));
        getProxy().getPluginManager().registerCommand(this, new ChatChannel.SlientMode(this));
        getProxy().getPluginManager().registerCommand(this, new ChatChannel.toggleGlobalChat(this));
        getProxy().getPluginManager().registerCommand(this, new ChatChannel.toggleBroadcastChat(this));
        getProxy().getPluginManager().registerCommand(this, new ChatChannel.toggleFilterNotify(this));

        getProxy().getPluginManager().registerCommand(this, new ChatChannel.DevChat(this));
        getProxy().getPluginManager().registerCommand(this, new ChatChannel.VIPChat(this));
        getProxy().getPluginManager().registerCommand(this, new ChatChannel.ModChat(this));
        getProxy().getPluginManager().registerCommand(this, new ChatChannel.UAChat(this));
        getProxy().getPluginManager().registerListener(this, new EventListener(this));
        //getProxy().getPluginManager().registerCommand(this, new ChatChannel.debugTest(this));

    }

    private void setupConfig() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                if (getResourceAsStream("config.yml") == null) {
                    System.out.println("Failed to obtain config.yml from getResourceAsStream(\"config.yml\")");
                }
                try (InputStream is = getResourceAsStream("config.yml");
                        OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }
    }

    private static void applyFilter(String messagea) {
        String oldMessage = messagea;
        String message = messagea;
        double numberOfUpperCase = 0;
        double numberOfUpperCaseWords = 0;
        String[] words = message.split(" ");
        for (String word : words) {
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
        if (numberOfUpperCase >= 4 && numberOfUpperCase / ((double) message.length()) >= 0.3 && message.length() >= 5 && numberOfUpperCaseWords / ((double) words.length) >= 0.4) {

            String output;
            String[] split = message.split(" ");

            split[0] = split[0].substring(0, 1).toUpperCase() + split[0].substring(1).toLowerCase();
            output = split[0];
            if (split.length >= 2) {
                for (int i = 1; i < split.length; i++) {
                    String string = split[i];
                    if (!"GODINACLOWNSUITE".contains(string)) {
                        string = string.toLowerCase();
                    }
                    output += " " + string;
                }
            }
            System.out.println("Please don't use too many capital letters.");

            String messageToSend = "james137137" + " was caught by the chat filter for: HIGHCAPS";
            System.out.println(messageToSend);
            System.out.println("Message was the following: " + oldMessage);
            System.out.println(output);
            return;
        }
        System.out.println(message);
    }

    private void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
        } catch (IOException ex) {
            Logger.getLogger(LolnetChatBC.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Configuration getConfig() {
        if (config != null) {
            return config;
        }
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                if (getResourceAsStream("config.yml") == null) {
                    System.out.println("Failed to obtain config.yml from getResourceAsStream(\"config.yml\")");
                }
                try (InputStream is = getResourceAsStream("config.yml");
                        OutputStream os = new FileOutputStream(configFile)) {
                    ByteStreams.copy(is, os);
                }
            } catch (IOException e) {
                throw new RuntimeException("Unable to create configuration file", e);
            }
        }
        try {
            config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
        } catch (IOException ex) {
            Logger.getLogger(LolnetChatBC.class.getName()).log(Level.SEVERE, null, ex);
        }
        return config;
    }

    public static String getIP() {
        try {
            // Construct data

            // Send data
            URL url = new URL("https://www.lolnet.co.nz/api/v1.0/getmyip.php");
            URLConnection conn = url.openConnection();
            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            //wr.write(data);
            wr.flush();

            // Get the response
            BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            String line;
            while ((line = rd.readLine()) != null) {
                return line;
            }
            wr.close();
            rd.close();
        } catch (Exception e) {
            return null;
        }

        return null;

    }

    private void setupLogger() {
        File theDir = new File(getDataFolder(), "ChatLog");

        // if the directory does not exist, create it
        if (!theDir.exists()) {
            theDir.mkdirs();
        }
        LolnetChatBC.myLogFile = new File(theDir, System.currentTimeMillis() + ".log");

        try {
            myLogFile.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(LolnetChatBC.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void reload() {
        setupLogger();
        config = getConfig();
        PVPServers = config.getStringList("PVPServers");
    }

}
