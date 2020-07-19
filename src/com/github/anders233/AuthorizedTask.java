package com.github.anders233;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.scheduler.PluginTask;

public class AuthorizedTask extends PluginTask<AuthorizedOP> {

    public AuthorizedTask(AuthorizedOP owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        for (Player player : Server.getInstance().getOnlinePlayers().values()) {
            if (player.isOp()) {
                if (!(AuthorizedOP.getInstance().getConfig().getStringList("获得OP授权的玩家").contains(player.getName().toLowerCase()))) {
                    player.setOp(false);
                    Server.getInstance().getLogger().warning("§a玩家：§a" + player.getName() + "§e没有获得OP授权，§d但ta却是一个OP，§a已撤销ta的OP权限");
                }
            }
        }
    }
}
