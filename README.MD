安卓小说阅读器
---

[![Hex.pm](https://img.shields.io/hexpm/l/plug.svg)](https://www.apache.org/licenses/LICENSE-2.0)
[![Language](https://img.shields.io/badge/Language-Kotlin-green)](https://kotlinlang.org/)

### 简介

这是一个安卓小说阅读器，最初作为我的本科毕设项目（2020届）,感谢各类开源项目的支持，让我顺利”毕业“。由于我是个实用主义者，该APP本质还是自编自用，所以会不定时维护。目前，该项目已经升级到 JDK 17，并且能够在 Android 15 上运行。

该项目的开发基于以下开源项目：

- [ZhangQinhao/MONKOVEL](https://github.com/ZhangQinhao/MONKOVEL)
- [mxdldev/android-mvp-mvvm-flytour](https://github.com/mxdldev/android-mvp-mvvm-flytour)

项目在架构上并没有完全整合，当前使用的是 MVVM 和 MVP 的混合架构，因此还有许多地方需要进一步改进。

### 项目维护与更新方向

1. 修复已知和未知的 bug；
2. 向 Kotlin 和 Jetpack Compose 迁移；
3. 移除 MVP 框架，全面迁移到 MVVM 架构，最终计划移除 `lib_book` 模块，将相关功能合并到业务模块或 `lib_book_common` 中；
4. 维护书源（目前部分书源不稳定，时常失效）；
5. 重写服务器端程序，并将其开源；
6. 增加一些有趣的小功能（概率较小）。

### 已知问题

1. 从 GreenDAO 迁移到 ObjectBox 时引发了以下两个问题：
   1. 打开书籍时，书籍会自动写入数据库；
   2. 书架的删除逻辑存在问题：由于从数据库中删除了书籍对象，如果想再次写入同一对象，ObjectBox 由于自身的限制，ID 相同的对象无法生效。

### 联系方式

如果您在使用过程中遇到任何问题，欢迎通过以下方式联系我：

- QQ: 572249217
- Email: 572249217@qq.com

**文档最后更新时间：2024年11月30日**
