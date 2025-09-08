package com.game.foresticefire.player;
//水女角色
// Watergirl.java
import javafx.scene.image.Image;

/**
 * 水女角色：
 * - 特色：可以安全接触水（不受伤），接触火会受伤或死亡
 */
public class Watergirl extends Player {

    public Watergirl(Image image, double startX, double startY, double w, double h) {
        super(image, startX, startY, w, h);
    }

    @Override
    protected void onEnterFire(double overlapArea) {
        // 水女碰火：直接死亡
        if (overlapArea > 0) {
            takeDamage(hp);
        }
    }

    @Override
    protected void onEnterWater(double overlapArea) {
        // Watergirl 在水中安全：可以回血或特殊机制
        // 示例：不做伤害
    }

    @Override
    protected void onDeath() {
        super.onDeath();
        // 例如触发 respawn
    }
}
