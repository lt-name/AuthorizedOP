package com.github.anders233;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.ConsoleCommandSender;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.utils.ConfigSection;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class AuthorizedOP extends PluginBase implements Listener {

    private static AuthorizedOP instance;
    private LinkedHashMap<String, String> aopMap = new LinkedHashMap<>();

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
        loadAuthorizedPlayers();
    }

    private void loadAuthorizedPlayers() {
        this.aopMap.clear();
        Object raw = this.getConfig().get("获得OP授权的玩家");
        if (raw instanceof java.util.List) {
            // 兼容旧版列表格式，自动迁移为UUID绑定格式
            java.util.List<?> list = (java.util.List<?>) raw;
            for (Object item : list) {
                String name = item.toString().toLowerCase();
                this.aopMap.put(name, "");
            }
            if (!list.isEmpty()) {
                saveAuthorizedPlayers();
                getLogger().info("§e已将旧版授权列表迁移为UUID绑定格式，UUID将在玩家首次登录时自动绑定");
            }
        } else {
            // 新版Map格式
            ConfigSection section = this.getConfig().getSection("获得OP授权的玩家");
            if (section != null) {
                for (String name : section.getKeys(false)) {
                    this.aopMap.put(name.toLowerCase(), section.getString(name, ""));
                }
            }
        }
    }

    private void saveAuthorizedPlayers() {
        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : this.aopMap.entrySet()) {
            map.put(entry.getKey(), entry.getValue());
        }
        this.getConfig().set("获得OP授权的玩家", map);
        this.getConfig().save();
    }

    @Override
    public void onEnable() {
        getLogger().info("§e======================");
        getLogger().info("§a授权OP系统开启 by Anders");
        getLogger().info("§b本插件针对有漏洞的插件");
        getLogger().info("§6防止玩家非法获取OP权限");
        getLogger().info("§d非本插件授权的OP一律无效");
        getLogger().info("§c已启用UUID绑定保护");
        getLogger().info("§e======================");
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getScheduler().scheduleRepeatingTask(this,
                new AuthorizedTask(this), getConfig().getInt("插件检测速度"));
    }

    public LinkedHashMap<String, String> getAopMap() {
        return this.aopMap;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String name = player.getName().toLowerCase();
        String uuid = player.getUniqueId().toString();

        if (this.aopMap.containsKey(name)) {
            String storedUuid = this.aopMap.get(name);
            if (storedUuid == null || storedUuid.isEmpty()) {
                // UUID尚未绑定，首次登录时自动绑定
                this.aopMap.put(name, uuid);
                saveAuthorizedPlayers();
                player.setOp(true);
                getLogger().info("§e已为授权玩家 §a" + name + " §e绑定UUID: §b" + uuid);
            } else if (storedUuid.equals(uuid)) {
                // UUID匹配，授予OP
                player.setOp(true);
            } else {
                // UUID不匹配！拒绝OP权限
                player.setOp(false);
                getServer().removeOp(name);
                getLogger().warning("§c警告：玩家 §a" + name + " §c的UUID不匹配！");
                getLogger().warning("§c授权UUID: §e" + storedUuid);
                getLogger().warning("§c当前UUID: §e" + uuid);
                getLogger().warning("§c已阻止该玩家获取OP权限（可能为冒名玩家）");
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (getConfig().getBoolean("是否开启指令管理")) {
            if (command.getName().equals("aop")) {
                if (sender instanceof ConsoleCommandSender) {
                    if (args.length > 0) {
                        switch (args[0]) {
                            default:
                            case "help":
                                sendHelp(sender);
                                break;
                            case "add":
                                if (args.length > 1) {
                                    String PlayerName = args[1].toLowerCase();
                                    if (this.aopMap.containsKey(PlayerName)) {
                                        sender.sendMessage("§d[授权] §e此玩家已在授权列表！");
                                    } else {
                                        Player player = Server.getInstance().getPlayer(PlayerName);
                                        if (player != null) {
                                            String uuid = player.getUniqueId().toString();
                                            this.aopMap.put(PlayerName, uuid);
                                            saveAuthorizedPlayers();
                                            player.setOp(true);
                                            sender.sendMessage("§d[授权] §e已将:" + PlayerName + "添加到授权列表，并设置为OP");
                                            sender.sendMessage("§d[授权] §e已绑定UUID: §b" + uuid);
                                        } else {
                                            this.aopMap.put(PlayerName, "");
                                            saveAuthorizedPlayers();
                                            sender.sendMessage("§d[授权] §e已将:" + PlayerName + "添加到授权列表");
                                            sender.sendMessage("§d[授权] §c注意: 该玩家不在线，UUID将在首次登录时绑定");
                                        }
                                    }
                                } else {
                                    sender.sendMessage("§d[授权] §a/aop §badd §d<玩家> §c- §e添加授权OP");
                                }
                                break;
                            case "del":
                                if (args.length > 1) {
                                    String PlayerName = args[1].toLowerCase();
                                    if (!aopMap.containsKey(PlayerName)) {
                                        sender.sendMessage("§d[授权] §e此玩家不在授权列表！");
                                    } else {
                                        Player player = Server.getInstance().getPlayer(PlayerName);
                                        this.aopMap.remove(PlayerName);
                                        saveAuthorizedPlayers();
                                        if (player != null) {
                                            player.setOp(false);
                                            sender.sendMessage("§d[授权] §e已将:" + PlayerName + "从授权列表删除，并删除OP");
                                        } else {
                                            sender.sendMessage("§d[授权] §e已将:" + PlayerName + "从授权列表删除");
                                        }
                                    }
                                } else {
                                    sender.sendMessage("§d[授权] §a/aop §bdel §d<玩家> §c- §e删除授权OP");
                                }
                                break;
                            case "list":
                                sender.sendMessage("§e==========§d[授权列表]§d==========");
                                if (this.aopMap.isEmpty()) {
                                    sender.sendMessage("\n§e暂无已授权的玩家\n");
                                } else {
                                    for (Map.Entry<String, String> entry : this.aopMap.entrySet()) {
                                        String uuidDisplay = entry.getValue().isEmpty() ? "§c未绑定" : "§b" + entry.getValue();
                                        sender.sendMessage("§e玩家：§a" + entry.getKey() + " §eUUID：" + uuidDisplay);
                                    }
                                }
                                sender.sendMessage("§b==========§d[授权列表]§a==========");
                                break;
                            case "rebind":
                                if (args.length > 1) {
                                    String PlayerName = args[1].toLowerCase();
                                    if (!aopMap.containsKey(PlayerName)) {
                                        sender.sendMessage("§d[授权] §e此玩家不在授权列表！");
                                    } else {
                                        Player player = Server.getInstance().getPlayer(PlayerName);
                                        if (player != null) {
                                            String uuid = player.getUniqueId().toString();
                                            this.aopMap.put(PlayerName, uuid);
                                            saveAuthorizedPlayers();
                                            sender.sendMessage("§d[授权] §e已重新绑定 " + PlayerName + " 的UUID: §b" + uuid);
                                        } else {
                                            this.aopMap.put(PlayerName, "");
                                            saveAuthorizedPlayers();
                                            sender.sendMessage("§d[授权] §e已清除 " + PlayerName + " 的UUID绑定，将在下次登录时重新绑定");
                                        }
                                    }
                                } else {
                                    sender.sendMessage("§d[授权] §a/aop §brebind §d<玩家> §c- §e重新绑定UUID");
                                }
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
                                loadAuthorizedPlayers();
                                sender.sendMessage("§d[授权] §e已重新加载配置文件");
                                break;

                        }
                    } else {
                        sendHelp(sender);
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
                        sendHelp(sender);
                    }
                } else {
                    sender.sendMessage("§d[授权] §e本指令只能在控制台使用！");
                }
            }
        }
        return true;
    }

    private static void sendHelp(CommandSender sender) {
        sender.sendMessage("§e==========§d[授权]§d==========");
        sender.sendMessage("§a/aop §badd §d<玩家> §c- §e添加授权OP");
        sender.sendMessage("§a/aop §bdel §d<玩家> §c- §e删除授权OP");
        sender.sendMessage("§a/aop §blist   §c- §e查看授权OP列表");
        sender.sendMessage("§a/aop §brebind §d<玩家> §c- §e重新绑定UUID");
        sender.sendMessage("§a/aop §bswitch   §c- §e开关授权指令");
        sender.sendMessage("§a/aop §breload   §c- §e重载配置文件");
        sender.sendMessage("§b==========§d[授权]§a==========");
    }
}
