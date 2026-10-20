# AL's Elevator for MTR — NeoForge 1.21.1

> **现代电梯** —— 基于 [Minecraft Transit Railway (MTR)](https://github.com/Minecraft-Transit-Railway/Minecraft-Transit-Railway) 的电梯专版。
>
> 感谢原作者 **Jonathan Ho 与 MTR 团队** 将 MTR 以 **MIT** 协议开源 —— 没有他们的工作就没有这个项目。

## 这是什么

本项目从 **MTR 官方源码(Minecraft 1.21.1 分支)** 干净重建,只保留电梯相关内容(lifts-only),面向 **Minecraft 1.21.1 + NeoForge 21.1**。

- 只注册电梯方块、物品、方块实体(注册白名单);
- 创造模式物品栏只有一个「电梯」标签页;
- 移除了全部铁路内容(轨道、列车、站台门、信号、售票系统等)及其音效/模型/贴图资源。

## 功能

- **立体轿厢**:移植了 MTR 1.20.4 的经典 `ModelLift1` 3D 轿厢模型(玻璃墙 + 金属边框 + 四角立柱 + 金属滑动门 + 天花板灯板),使用原版 `lift_1.png` 贴图;
- **电梯门联动**:到站自动开门,轿厢门与门方块同步开合;
- **分侧开门**:双面轿厢只打开**放置了电梯门方块的那一侧**;单面轿厢背面门方块保持关闭;
- **开门提示音**:到站开门瞬间播放一声「叮」;
- **乘坐**:站进轿厢即跟随移动,四面空气墙;正面/背面门洞在门开启时均可进出;
- **轿厢内面板**:按 `Z` 打开楼层选择面板;
- **电梯配置界面**:尺寸、偏移、双面、样式、旋转,勾选状态正确保存;
- **楼层显示面板**:屏显数字与轿厢门对齐;
- **完整中文汉化**(简体 / 繁体 / 粤语);
- **模组信息署名**:基于 MTR 开源构建。

## 安装

1. 安装 **Minecraft 1.21.1** + **NeoForge 21.1.x**;
2. 把 `Modern-Elevators-Escalators-neoforge-1.21.1-*.jar` 放进 `mods/` 目录;
3. 启动游戏即可。

## 从源码构建

**注意**:本仓库不包含 MTR 的完整源码,只包含本项目的改动与构建流水线。构建时:

1. 下载 MTR 官方源码(1.21.1 分支);
2. 按 `rebuild/` 中的脚本执行预处理与编译(详见各脚本头部注释);
3. 用 `rebuild/package.py` 打包。

构建流水线(因网络环境不使用 Gradle):

| 脚本 | 作用 |
|---|---|
| `rebuild/preprocess.py` | 解析 stonecutter `//? if` 条件编译 |
| `rebuild/relocate.py` | 依赖包名重定位 |
| `rebuild/ApplyAT.java` | 应用 NeoForge Access Transformer |
| `rebuild/RepairKotlin.java` | 修复 shadow 的 kotlin 包名破坏 |
| `rebuild/compile_all.ps1` | javac + Lombok 编译 |
| `rebuild/package.py` | 打包(替换类、汉化、贴图、mods.toml) |
| `rebuild/gen_logo.py` | 生成模组图标 |

## 许可证与署名

- 本项目**代码**基于 **Minecraft Transit Railway**,原项目以 **MIT** 协议发布;
- MTR 版权归 **Jonathan Ho 与 MTR 团队** 所有;
- 本项目为**衍生作品**,同样以 **MIT** 协议开源(见 `LICENSE`);
- 如果你二次修改/分发,请保留上述署名与 MIT 协议声明。

> Minecraft Transit Railway: https://github.com/Minecraft-Transit-Railway/Minecraft-Transit-Railway
