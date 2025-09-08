软件应用设计
# 实训项目
项目名称；森林冰火人游戏

项目分工
素材资源整合及ui设计：周靖宇
音效及关卡设计：何承祖
代码负责人：王适涵 马子周
项目优化测试及代码整合：王仕汪
（每个人可根据项目完成进度合理完成其他工作）

#项目结构：
└─ src
│     └─ main
│        ├─ java
│        │  └─ com.game.foresticefire
│        │     ├─ GameApp.java        ← JavaFX 启动入口
│        │     ├─ GameLauncher.java   ← main() 方法
│        │     ├─ GameController.java ← 全局导演类
│        │     ├─ entity
│        │     │  ├─ Player.java      ← 冰/火人实体
│        │     │  └─ Element.java     ← 水晶、按钮、门等
│        │     ├─ level
│        │     │  ├─ Tile.java        ← 地砖（ lava / water / wall …）
│        │     │  ├─ Level.java       ← 一关的数据
│        │     │  └─ LevelLoader.java ← 从 txt / json 读关卡
│        │     ├─ input
│        │     │  └─ KeyInput.java    ← 键盘事件总线
│        │     ├─ physics
│        │     │  └─ Engine.java      ← AABB 碰撞 + 重力
│        │     └─ render
│        │        ├─ Sprite.java      ← 贴图包装
│        │        └─ Renderer.java    ← 画布渲染
│        └─ resources
 
第一天工作概要：
1.搜索资源
2.设计音效
3.配置相关环境
4.完善项目结构及对应的接口
