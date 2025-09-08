package com.game.foresticefire.player;

// Fireboy.java
import javafx.scene.image.Image;

/**
 * 火男角色：
 * - 特色：可以安全接触火（不受伤），触碰水会受伤或死亡（视 overlapped area）
 * - 可以覆盖 onEnterFire/onEnterWater 做差异化音效/特效
 */
public class Fireboy extends Player {

    public Fireboy(Image image, double startX, double startY, double w, double h) {
        super(image, startX, startY, w, h);
        // 火男可能在火中回血或者不受伤（示例：不受火伤害）
    }

    @Override
    protected void onEnterFire(double overlapArea) {
        // Fireboy 在火中安全：可做加成（恢复）或特效
        // 示例：如果重叠面积非常大，恢复一点 HP（可定制）
        if (overlapArea > 0.5) { // overlapArea 范围：0..1，示例阈值
            // 小幅恢复（每帧不要恢复太多，真实游戏应按时间累计）
            // 这里示例不自动恢复，放钩子
        }
        // 不做伤害
    }

    @Override
    protected void onEnterWater(double overlapArea) {
        // 火娃碰水：直接死亡
        if (overlapArea > 0) {
            takeDamage(hp);
        }
    }

    @Override
    protected void onDeath() {
        super.onDeath();
        // 你可以在这里触发重生、播放死亡动画、发出事件给 GameManager 等
    }
}
