package ee.lutsu.alpha.mc.mytown.event.prot;

import java.lang.reflect.Method;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityTNTPrimed;
import ee.lutsu.alpha.mc.mytown.MyTown;
import ee.lutsu.alpha.mc.mytown.MyTownDatasource;
import ee.lutsu.alpha.mc.mytown.entities.TownBlock;
import ee.lutsu.alpha.mc.mytown.event.ProtBase;

public class LOTR extends ProtBase {
    public static LOTR instance = new LOTR();

    private Class<?> clOrcBomb = null;
    private Method mGetBombStrengthLevel;

    @Override
    public void load() throws Exception {
    	clOrcBomb = Class.forName("lotr.common.LOTREntityOrcBomb");
        mGetBombStrengthLevel = clOrcBomb.getDeclaredMethod("getBombStrengthLevel");
    }

    @Override
    public boolean loaded() {
    	return clOrcBomb != null;
    }

    @Override
    public boolean isEntityInstance(Entity e) {
        return clOrcBomb.isInstance(e);
    }

    @Override
    public String update(Entity e) throws Exception {
        EntityTNTPrimed tnt = (EntityTNTPrimed) e;

        if (tnt.isDead || tnt.fuse > 1) {
            return null;
        }

        int orcBombStrength = ((Integer) mGetBombStrengthLevel.invoke(e)) + 1;	
        int	explosionRadius = orcBombStrength * 4;
        
        int radius = explosionRadius + 1; // 1 for safety

        int x1 = (int) e.posX - radius >> 4;
        int z1 = (int) e.posZ - radius >> 4;
        int x2 = (int) e.posX + radius >> 4;
        int z2 = (int) e.posZ + radius >> 4;

        boolean canBlow = true;
        for (int x = x1; x <= x2 && canBlow; x++) {
            for (int z = z1; z <= z2 && canBlow; z++) {
                if (!canBlow(e.dimension, x << 4, (int) e.posY - radius, (int) e.posY + radius, z << 4)) {
                    canBlow = false;
                }
            }
        }

        return canBlow ? null : "TNT explosion disabled here";
    }

    private boolean canBlow(int dim, int x, int yFrom, int yTo, int z) {
        TownBlock b = MyTownDatasource.instance.getPermBlockAtCoord(dim, x, yFrom, yTo, z);

        if (b == null || b.town() == null) {
            return !MyTown.instance.getWorldWildSettings(dim).disableTNT;
        }

        return !b.settings.disableTNT;
    }

    @Override
    public String getMod() {
        return "Lord of The Rings";
    }

    @Override
    public String getComment() {
        return "Build check: LOTREntityOrcBomb";
    }
}
