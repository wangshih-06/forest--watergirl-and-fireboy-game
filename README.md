软件应用设计
# 实训项目
项目名称；森林冰火人游戏

项目分工
素材资源整合及ui设计：周靖宇
音效及关卡设计：何承祖
代码负责人： 马子周 王仕汪 王适涵
项目优化测试及代码整合：王仕汪
（每个人可根据项目完成进度合理完成其他工作）

#项目结构：
forest-fireboy-watergirl/
│── src/main/java/com/game/
│   │── Main.java                  // 程序入口
│   │
│   ├── core/
│   │   ├── GameEngine.java        // 游戏主循环、场景管理
│   │   ├── GameConfig.java        // 全局常量配置（重力、速度、地图大小等）
│   │   └── GameController.java    // 游戏控制器（处理事件）
│   │
│   ├── model/
│   │   ├── Character.java         // 抽象角色类（通用移动、跳跃、碰撞）
│   │   ├── Fireboy.java           // 火人类
│   │   ├── Watergirl.java         // 冰人类
│   │   ├── Diamond.java           // 钻石
│   │   ├── Lever.java             // 拉杆
│   │   ├── Door.java              // 火门 & 水门
│   │   └── Platform.java          // 地图中的平台、陷阱
│   │
│   ├── events/
│   │   ├── GameEvent.java         // 游戏事件基类
│   │   ├── CollisionEvent.java    // 碰撞事件
│   │   ├── DiamondEvent.java      // 钻石拾取事件
│   │   ├── LeverEvent.java        // 拉杆事件
│   │   ├── DoorEvent.java         // 角色进入门事件
│   │   └── EventListener.java     // 事件监听接口
│   │
│   ├── ui/
│   │   ├── GameView.java          // 渲染游戏场景
│   │   ├── HUD.java               // 显示得分/剩余时间/提示
│   │   └── SpriteLoader.java      // 加载 Fireboy/Watergirl 图片
│   │
│   └── utils/
│       ├── CollisionDetector.java // 碰撞检测
│       └── SoundManager.java      // 音效管理
│
└── resources/
   ├── maps/                      // 关卡地图文件(JSON/TMX)
   ├── sounds/                    // 音效
   └── images/                    // Fireboy, Watergirl, 


 一、整体架构思路

整个游戏采用 分层架构：
core 层：游戏引擎（GameEngine）、控制器（GameController）、配置（GameConfig）
负责主循环、键盘事件、角色控制、关卡逻辑。
model 层：游戏对象（角色、钻石、门、拉杆、平台）
封装游戏实体对象，定义其状态（位置、速度）和行为（移动、拾取、触发）。
events 层：事件系统
使用 观察者模式，将“游戏逻辑”与“事件处理”解耦。
例如：角色碰到钻石时 → 触发 DiamondEvent → 分发给监听器。
ui 层：用户界面（GameView, HUD）
负责渲染游戏画面和 HUD（分数、时间、提示）。
utils 层：工具类（碰撞检测、音效管理等）
提供公共服务，避免逻辑重复。

二、运行逻辑流程
Main.java 启动程序 初始化 JavaFX Stage 和 Scene
创建 GameEngine，加载地图与角色
GameEngine 主循环
每帧更新角色位置、检测碰撞
触发事件（例如角色碰到钻石 → DiamondEvent）
通知 GameView 更新画面
事件分发机制
通过 EventManager 实现观察者模式：
触发者：角色、物体等
事件：CollisionEvent, DiamondEvent, LeverEvent, DoorEvent
监听器：GameController、UI、音效管理器等
玩家操作输入
GameController 监听键盘事件
Fireboy：A, D, W → 左右移动 / 跳跃
Watergirl：←, →, ↑ → 左右移动 / 跳跃
三、接口设计
1. Character（抽象类）
设计理由：Fireboy 和 Watergirl 有很多共同点（移动、跳跃、重力），所以抽象出一个父类。
接口职责：定义角色的基础行为。
public abstract class Character {
    void moveLeft();
    void moveRight();
    void jump();
    void update(double gravity);
    Bounds getBounds();

    abstract void onDiamondPickup();
    abstract void onLeverActivate();
    abstract void onEnterDoor();
}

2. EventListener（接口）
设计理由：让不同模块都能订阅游戏事件（UI 可以显示提示，音效模块可以播放音效）。
职责：抽象出一个事件处理方法。
@FunctionalInterface
public interface EventListener {
    void handle(GameEvent event);
}

3. GameEvent（抽象事件类）
设计理由：所有事件都共享一个父类，便于扩展。
职责：定义通用事件信息（时间戳、类型）。
public abstract class GameEvent {
    private final long timestamp = System.currentTimeMillis();
    public abstract String getType();
}

4. EventManager（事件分发器）

设计理由：使用观察者模式，管理事件监听器。
职责：负责注册监听器 & 广播事件。
public class EventManager {
    void register(EventListener listener);
    void unregister(EventListener listener);
    void fireEvent(GameEvent event);
}

5. 各种事件接口实现

CollisionEvent：角色碰撞到物体
DiamondEvent：角色拾取钻石
LeverEvent：角色触发拉杆
DoorEvent：角色进入门
👉 统一继承 GameEvent，并携带不同的上下文信息（角色、物体）。
6. GameController（控制器）
设计理由：解耦输入事件与角色行为。
职责：把键盘输入映射到 Fireboy & Watergirl 的行为。
public class GameController {
    void bindKeyEvents(Scene scene);
    void onDiamondCollected(Diamond diamond);
}
7. GameEngine（主循环）
设计理由：统一调度更新、事件检测、渲染。
职责：
更新角色状态（重力、移动）
检测碰撞（钻石、拉杆、门）
触发对应事件
调用 GameView 渲染
四、接口设计总结

角色接口（抽象类 Character）：屏蔽角色差异，定义统一 API。
事件接口（EventListener + GameEvent）：采用观察者模式，解耦逻辑与响应。
引擎接口（GameEngine + GameController）：控制循环与输入，作为系统“大脑”。
UI 接口（GameView）：只关心如何渲染，不关心业务逻辑。
这样设计的好处：
高内聚，低耦合 → 角色逻辑、事件分发、UI 渲染各自独立。
易扩展 → 想增加“开关机关事件”或“陷阱事件”，只需增加一个新事件类。
多监听器支持 → UI、音效、分数统计模块都能订阅同一个事件。

 
第一天工作概要：
1.搜索资源
2.设计音效
3.配置相关环境
4.完善项目结构及对应的接口
