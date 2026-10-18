package com.delphy.legendmagic.util;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

/**
 * 魔法の計算・発射軌道・判定に関するユーティリティクラス。
 */
public class MagicUtil {

    /**
     * 術者の魔法射出方向ベクトルを計算する。
     * 術者がMobでありターゲットが存在する場合は、ターゲットの頭部中心へ向かうベクトルを優先して返す。
     * プレイヤーまたはターゲット無しの場合は、術者の視線方向（getLookAngle）を返す。
     *
     * @param caster 術者
     * @param origin 発射始点（頭部や魔方陣の中心）
     * @return 正規化された射出方向ベクトル
     */
    public static Vec3 getSpellAimDirection(LivingEntity caster, Vec3 origin) {
        if (caster instanceof Mob mob && mob.getTarget() != null) {
            Vec3 targetEye = mob.getTarget().getEyePosition();
            Vec3 diff = targetEye.subtract(origin);
            if (diff.lengthSqr() > 0.0001D) {
                return diff.normalize();
            }
        }
        return caster.getLookAngle();
    }
}
