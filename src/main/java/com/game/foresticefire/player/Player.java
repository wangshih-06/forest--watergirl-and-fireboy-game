package com.game.foresticefire.player;

// Player.java  （抽象基类）
import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

/**
 * 抽象 Player 基类：封装位置、速度、碰撞检测、拾取交互、进入环境触发等逻辑。
 * - 作为 JavaFX Node 使用（extends Group），内部包含 ImageView（角色贴图）和一个碰撞箱 Rectangle（可视化/调试可选）。
 * - 运动与物理通过 update(dt) 驱动（dt 单位：秒）。
 */
public abstract class Player extends Group {
    // --- 可调参数 ---
    protected static final double GRAVITY = 1600; // px/s^2
    protected static final double MOVE_SPEED = 220; // px/s
    protected static final double JUMP_SPEED = 560; // 初始跳跃速度 px/s
    protected static final double MAX_FALL_SPEED = 1000; // 限制下落速度

    // --- 状态 ---
    protected double vx = 0;
    protected double vy = 0;
    protected boolean facingRight = true;
    protected boolean onGround = false;
    protected boolean canDoubleJump = false; // 可选：是否允许二段跳
    protected int hp = 3; // 生命值，可在子类改动

    // JavaFX 视觉
    protected ImageView sprite;
    protected Rectangle hitBox; // 简单 AABB 碰撞箱（相对于 Group 的坐标系）

    // for debug 可见边界
    protected boolean debugHitBoxVisible = false;

    // 身体尺寸（基于贴图/贴图外框）
    protected double width;
    protected double height;

    public Player(Image image, double startX, double startY, double width, double height) {
        this.width = width;
        this.height = height;

        sprite = new ImageView(image);
        sprite.setFitWidth(width);
        sprite.setFitHeight(height);
        this.getChildren().add(sprite);

        hitBox = new Rectangle(0, 0, width, height);
        hitBox.setFill(Color.color(1,0,0,0.0)); // 默认透明
        hitBox.setStroke(Color.color(1,0,0,0.0));
        if (debugHitBoxVisible) {
            hitBox.setFill(Color.color(1, 0, 0, 0.2));
            hitBox.setStroke(Color.YELLOW);
        }
        this.getChildren().add(hitBox);

        this.setTranslateX(startX);
        this.setTranslateY(startY);
    }

    // --- 输入控制 API（外部键盘处理调用） ---
    public void moveLeft() {
        vx = -MOVE_SPEED;
        facingRight = false;
        updateSpriteFlip();
    }

    public void moveRight() {
        vx = MOVE_SPEED;
        facingRight = true;
        updateSpriteFlip();
    }

    public void stopMoving() {
        vx = 0;
    }

    public void tryJump() {
        if (onGround) {
            vy = -JUMP_SPEED;
            onGround = false;
            canDoubleJump = true;
            onJumped();
        } else if (canDoubleJump) {
            vy = -JUMP_SPEED * 0.9; // 二段跳力度略小
            canDoubleJump = false;
            onJumped();
        }
    }

    // 子类可以覆盖
    protected void onJumped() {}

    // 角色进入火或水的处理由子类实现
    protected abstract void onEnterFire(double overlapArea);
    protected abstract void onEnterWater(double overlapArea);

    // 被钻石拾取回调
    protected void onPickedDiamond(Diamond d) {
        // 默认：增加分数、播放音效等（由游戏层实现）
        d.setCollected(true);
    }

    // 与拉杆交互
    public void interactLever(Lever lever) {
        if (lever == null) return;
        lever.toggle();
    }

    // 受伤处理（可以被子类覆盖）
    protected void takeDamage(int amount) {
        hp -= amount;
        if (hp <= 0) {
            onDeath();
        } else {
            onHurt();
        }
    }

    protected void onHurt() {
        // 临时无敌、闪烁等逻辑可放这里
    }

    protected void onDeath() {
        // 默认复位或通知游戏管理器
        // 子类或游戏层应覆盖（或监听状态）
        System.out.println(this.getClass().getSimpleName() + " died.");
        // 简单复位示例：放到初始位置（真实游戏中应调用 gameManager）
    }

    // --- 更新（必须由游戏主循环调用，dt 秒） ---
    public void update(double dt, GameMap map, List<Diamond> diamonds, List<Lever> levers) {
        // 1) 应用重力
        vy += GRAVITY * dt;
        if (vy > MAX_FALL_SPEED) vy = MAX_FALL_SPEED;

        // 2) 预测位移
        double nextX = getTranslateX() + vx * dt;
        double nextY = getTranslateY() + vy * dt;

        // 3) 水平碰撞检测（地图）
        Bounds futureHB = new Rectangle(nextX, getTranslateY(), width, height).getBoundsInLocal();
        // 针对 tile-based map，查询可能碰撞瓦片集合
        List<Tile> collidingX = map.getPotentialCollidingTiles(nextX, getTranslateY(), width, height);
        boolean collidedX = false;
        for (Tile t : collidingX) {
            if (!t.isPassable() && intersects(nextX, getTranslateY(), width, height, t)) {
                collidedX = true;
                break;
            }
        }
        if (!collidedX) {
            setTranslateX(nextX);
        } else {
            vx = 0; // 碰撞，水平速度归零
        }

        // 4) 垂直碰撞（地面/天花板/危险区）
        List<Tile> collidingY = map.getPotentialCollidingTiles(getTranslateX(), nextY, width, height);
        boolean collidedY = false;
        for (Tile t : collidingY) {
            if (!t.isPassable() && intersects(getTranslateX(), nextY, width, height, t)) {
                // 如果向下撞到地面（vy>0），把角色贴到地面之上
                if (vy > 0) {
                    setTranslateY(t.getY() - height);
                    onGround = true;
                    vy = 0;
                    canDoubleJump = false;
                } else if (vy < 0) {
                    // 碰到天花板
                    setTranslateY(t.getY() + t.getHeight());
                    vy = 0;
                }
                collidedY = true;
                break;
            }
        }
        if (!collidedY) {
            setTranslateY(nextY);
            onGround = false;
        }

        // 5) 地图边缘检测（防止跑出地图）
        if (getTranslateX() < map.getLeftBound()) {
            setTranslateX(map.getLeftBound());
            vx = 0;
        } else if (getTranslateX() + width > map.getRightBound()) {
            setTranslateX(map.getRightBound() - width);
            vx = 0;
        }
        if (getTranslateY() < map.getTopBound()) {
            setTranslateY(map.getTopBound());
            vy = 0;
        } else if (getTranslateY() + height > map.getBottomBound()) {
            // 掉出地图底部：通常为死亡
            setTranslateY(map.getBottomBound() - height);
            vy = 0;
            onGround = true;
            // 如果是掉入深渊，视为死亡（由子类处理）
            onFallOutOfMap();
        }

        // 6) 与钻石检测（AABB）
        for (Diamond d : diamonds) {
            if (!d.isCollected() && intersects(this.getTranslateX(), this.getTranslateY(), width, height, d)) {
                onPickedDiamond(d);
            }
        }

        // 7) 与拉杆交互检测（如果需要触发靠近自动交互）
        for (Lever lever : levers) {
            if (intersects(this.getTranslateX(), this.getTranslateY(), width, height, lever.getBounds())) {
                // 这里只是示例：需要手动触发交互的可以通过键盘事件调用 interactLever(lever)
                // 或者在靠近时自动提示/高亮
            }
        }

        // 8) 与危险（火/水）检测：map 提供 getOverlappingEnvironmentAreas(playerBounds)
        List<GameMap.EnvironmentOverlap> envs = map.getEnvironmentOverlaps(getTranslateX(), getTranslateY(), width, height);
        for (GameMap.EnvironmentOverlap ev : envs) {
            if (ev.environment == GameMap.Environment.FIRE) {
                onEnterFire(ev.overlapArea);
            } else if (ev.environment == GameMap.Environment.WATER) {
                onEnterWater(ev.overlapArea);
            }
        }
    }

    protected void onFallOutOfMap() {
        // 掉出地图：通常死亡
        takeDamage(hp); // 直接扣光
    }

    // --- 简单 AABB 帮助函数 ---
    protected boolean intersects(double ax, double ay, double aw, double ah, Tile tile) {
        return !(ax + aw <= tile.getX() || ax >= tile.getX() + tile.getWidth() || ay + ah <= tile.getY() || ay >= tile.getY() + tile.getHeight());
    }

    protected boolean intersects(double ax, double ay, double aw, double ah, Diamond d) {
        return !(ax + aw <= d.getX() || ax >= d.getX() + d.getWidth() || ay + ah <= d.getY() || ay >= d.getY() + d.getHeight());
    }

    protected boolean intersects(double ax, double ay, double aw, double ah, Bounds bounds) {
        return !(ax + aw <= bounds.getMinX() || ax >= bounds.getMaxX() || ay + ah <= bounds.getMinY() || ay >= bounds.getMaxY());
    }

    // getter/setter
    public int getHp() { return hp; }
    public void setHp(int hp) { this.hp = hp; }

    protected void updateSpriteFlip() {
        sprite.setScaleX(facingRight ? 1 : -1);
    }
}

// ----------------- 接口定义：供 Player 依赖，外部自行提供实现 -----------------
interface GameMap {
	enum Environment { FIRE, WATER }

	class EnvironmentOverlap {
		public final Environment environment;
		public final double overlapArea; // 0..1 之间
		public EnvironmentOverlap(Environment environment, double overlapArea) {
			this.environment = environment;
			this.overlapArea = overlapArea;
		}
	}

	double getLeftBound();
	double getRightBound();
	double getTopBound();
	double getBottomBound();

	java.util.List<Tile> getPotentialCollidingTiles(double x, double y, double w, double h);
	java.util.List<GameMap.EnvironmentOverlap> getEnvironmentOverlaps(double x, double y, double w, double h);
}

interface Tile {
	boolean isPassable();
	double getX();
	double getY();
	double getWidth();
	double getHeight();
}

interface Diamond {
	boolean isCollected();
	void setCollected(boolean collected);
	double getX();
	double getY();
	double getWidth();
	double getHeight();
}

interface Lever {
	void toggle();
	Bounds getBounds();
}
