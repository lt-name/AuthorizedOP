package com.github.anders233;

import cn.nukkit.scheduler.PluginTask;

import java.util.Map;

public class AuthorizedTask extends PluginTask<AuthorizedOP> {

    public AuthorizedTask(AuthorizedOP owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        for (Map.Entry<String, Object> entry : owner.getServer().getOps().getAll().entrySet()) {
            if (!owner.getAopList().contains(entry.getKey().toLowerCase())) {
                try {
                    owner.getServer().getPlayer(entry.getKey()).setOp(false);
                } catch (Exception ignored) {

                }
                owner.getServer().removeOp(entry.getKey());
                owner.getLogger().warning("§a玩家：§a" + entry.getKey() + "§e没有获得OP授权，§d但ta却是一个OP，§a已撤销ta的OP权限");
            }
        }
/*        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
            if (player.isOp()) {
                if (!(owner.getConfig().getStringList("获得OP授权的玩家").contains(player.getName().toLowerCase()))) {
                    player.setOp(false);
                    owner.getServer().removeOp(player.getName());
                    owner.getLogger().warning("§a玩家：§a" + player.getName() + "§e没有获得OP授权，§d但ta却是一个OP，§a已撤销ta的OP权限");
                }
            }
        }*/
    }
}
