package com.github.anders233;

import cn.nukkit.Player;
import cn.nukkit.scheduler.PluginTask;

import java.util.Map;

public class AuthorizedTask extends PluginTask<AuthorizedOP> {

    public AuthorizedTask(AuthorizedOP owner) {
        super(owner);
    }

    @Override
    public void onRun(int i) {
        for (Map.Entry<String, Object> entry : owner.getServer().getOps().getAll().entrySet()) {
            String opName = entry.getKey().toLowerCase();
            if (!owner.getAopMap().containsKey(opName)) {
                // 未在授权列表中，撤销OP
                try {
                    owner.getServer().getPlayer(entry.getKey()).setOp(false);
                } catch (Exception ignored) {

                }
                owner.getServer().removeOp(entry.getKey());
                owner.getLogger().warning("§a玩家：§a" + entry.getKey() + "§e没有获得OP授权，§d但ta却是一个OP，§a已撤销ta的OP权限");
            } else {
                // 在授权列表中，检查在线玩家的UUID是否匹配
                Player player = owner.getServer().getPlayer(entry.getKey());
                if (player != null) {
                    String storedUuid = owner.getAopMap().get(opName);
                    if (storedUuid != null && !storedUuid.isEmpty()) {
                        String playerUuid = player.getUniqueId().toString();
                        if (!storedUuid.equals(playerUuid)) {
                            player.setOp(false);
                            owner.getServer().removeOp(entry.getKey());
                            owner.getLogger().warning("§c玩家：§a" + entry.getKey() + " §cUUID不匹配（授权: " + storedUuid + " / 实际: " + playerUuid + "），§a已撤销OP权限");
                        }
                    }
                }
            }
        }
    }
}
