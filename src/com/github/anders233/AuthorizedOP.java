package com.github.anders233;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;

import java.io.File;
import java.util.List;

public class AuthorizedOP extends PluginBase implements Listener {

    private static AuthorizedOP instance;

    static AuthorizedOP getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
        File Config = new File(this.getDataFolder() + "/config.yml");
        if (!Config.exists()) {
            saveDefaultConfig();
            reloadConfig();
        }
    }

    @Override
    public void onEnable() {
        getLogger().info("§e======================");
        getLogger().info("§a授权OP系统开启 by Anders");
        getLogger().info("§b本插件针对有漏洞的插件");
        getLogger().info("§6防止玩家非法获取OP权限");
        getLogger().info("§d非本插件授权的OP一律无效");
        getLogger().info("§e======================");
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleRepeatingTask(new AuthorizedTask(), getConfig().getInt("插件检测速度"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        List<String> AopList = getConfig().getStringList("获得OP授权的玩家");
        if (getConfig().getBoolean("是否开启指令管理")) {
            if (command.getName().equals("aop")) {
                if (sender instanceof ConsoleCommandSender) {
                    if (args.length > 0) {
                        switch (args[0]) {
                            default:
                            case "help":
                                sendHelp((ConsoleCommandSender) sender);
                                break;
                            case "add":
                                if (args.length > 1) {
                                    String PlayerName = args[1].toLowerCase();
                                    if (AopList.contains(PlayerName)) {
                                        sender.sendMessage("§d[授权] §e此玩家已在授权列表！");
                                    } else {
                                        Player player = Server.getInstance().getPlayer(PlayerName);
                                        if (player != null) {
                                            AopList.add(PlayerName);
                                            getConfig().set("获得OP授权的玩家", AopList);
                                            getConfig().save();
                                            player.setOp(true);
                                            sender.sendMessage("§d[授权] §e已将:" + PlayerName + "添加到授权列表，并设置为OP");
                                        } else {
                                            AopList.add(PlayerName);
                                            getConfig().set("获得OP授权的玩家", AopList);
                                            getConfig().save();
                                            sender.sendMessage("§d[授权] §e已将:" + PlayerName + "添加到授权列表");
                                        }
                                    }
                                } else {
                                    sender.sendMessage("§d[授权] §a/aop §badd §d<玩家> §c- §e添加授权OP");
                                }
                                break;
                            case "del":
                                if (args.length > 1) {
                                    String PlayerName = args[1].toLowerCase();
                                    if (!(AopList.contains(PlayerName))) {
                                        sender.sendMessage("§d[授权] §e此玩家不授权列表！");
                                    } else {
                                        Player player = Server.getInstance().getPlayer(PlayerName);
                                        if (player != null) {
                                            AopList.remove(PlayerName);
                                            getConfig().set("获得OP授权的玩家", AopList);
                                            getConfig().save();
                                            player.setOp(false);
                                            sender.sendMessage("§d[授权] §e已将:" + PlayerName + "从授权列表删除，并删除OP");
                                        } else {
                                            AopList.remove(PlayerName);
                                            getConfig().set("获得OP授权的玩家", AopList);
                                            getConfig().save();
                                            sender.sendMessage("§d[授权] §e已将:" + PlayerName + "从授权列表删除");
                                        }
                                    }
                                } else {
                                    sender.sendMessage("§d[授权] §a/aop §badd §d<玩家> §c- §e添加授权OP");
                                }
                                break;
                            case "list":
                                sender.sendMessage("§e==========§d[授权列表]§d==========");
                                if (AopList.isEmpty()) {
                                    sender.sendMessage("\n§e暂无已授权的玩家\n");
                                } else {
                                    for (String players : AopList) {
                                        sender.sendMessage("§e玩家：§a" + players);
                                    }
                                }
                                sender.sendMessage("§b==========§d[授权列表]§a==========");
                                break;
                            case "switch":
                                if (getConfig().getBoolean("是否开启指令管理")) {
                                    getConfig().set("是否开启指令管理", false);
                                    getConfig().save();
                                    sender.sendMessage("§d[授权] §e已关闭指令管理");
                                } else {
                                    getConfig().set("是否开启指令管理", true);
                                    getConfig().save();
                                    sender.sendMessage("§d[授权] §e已开启指令管理");
                                }
                                break;
                            case "reload":
                                getConfig().reload();
                                sender.sendMessage("§d[授权] §e已重新加载配置文件");
                                break;

                        }
                    } else {
                        sendHelp((ConsoleCommandSender) sender);
                    }
                } else {
                    sender.sendMessage("§d[授权] §e本指令只能在控制台使用！");
                }
            }
        } else {
            if (command.getName().equals("aop")) {
                if (sender instanceof ConsoleCommandSender) {
                    if (args.length > 0) {
                        if (args[0].equals("switch")) {
                            if (getConfig().getBoolean("是否开启指令管理")) {
                                getConfig().set("是否开启指令管理", false);
                                getConfig().save();
                                sender.sendMessage("§d[授权] §e已关闭指令管理");
                            } else {
                                getConfig().set("是否开启指令管理", true);
                                getConfig().save();
                                sender.sendMessage("§d[授权] §e已开启指令管理");
                            }
                        }
                    } else {
                        sendHelp((ConsoleCommandSender) sender);
                    }
                } else {
                    sender.sendMessage("§d[授权] §e本指令只能在控制台使用！");
                }
            }
        }
        return true;
    }

    private static void sendHelp(ConsoleCommandSender sender) {
        sender.sendMessage("§e==========§d[授权]§d==========");
        sender.sendMessage("§a/aop §badd §d<玩家> §c- §e添加授权OP");
        sender.sendMessage("§a/aop §bdel §d<玩家> §c- §e删除授权OP");
        sender.sendMessage("§a/aop §blist   §c- §e查看授权OP列表");
        sender.sendMessage("§a/aop §bswitch   §c- §e开关授权指令");
        sender.sendMessage("§a/aop §breload   §c- §e重载配置文件");
        sender.sendMessage("§b==========§d[授权]§a==========");
    }
}
