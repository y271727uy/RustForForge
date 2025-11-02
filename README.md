# Rust for Forge Mod

一个为Minecraft Forge模组提供高性能Rust接口的模组。

## ⚠️ 重要警告：实验性模组

Rust for Forge 是一个**实验性模组**，目前仍处于开发和测试阶段。使用此模组可能会遇到以下问题：

1. 功能不稳定或存在未知bug
2. 性能可能未达到预期
3. API 可能在未来版本中发生重大变化
4. Rust 崩溃或 panic 的风险由调用者承担
5. 本库1.0.0版本采用较为激进的rust在JVM中直接交互，若rust崩溃则JVM也会一并崩溃。

请开发者在使用过程中积极反馈问题和建议，帮助我们改进项目。

## 项目概述

Rust for Forge 是一个为 Minecraft Forge 模组提供高性能 Rust 接口的库，通过 JNI 实现 Java 与 Rust 的无缝交互。

## 技术栈版本信息

- **Java 版本**：Java 17 (JVM 17.0.11)
- **Rust 版本**：2021 edition
- **构建工具**：Gradle 8.8
- **Forge 版本**：根据 build.gradle 中的配置

## 核心特性

### 极致性能
- Rust 与 JVM 共享同一进程
- 直接内存访问，零拷贝数据交换
- 直接 JNI 调用，无中间层开销
- 最小化抽象层和缓冲区

### 安全的错误处理
- Rust panic 被捕获并转换为 Java 异常
- 避免 JVM 因 Rust 崩溃而完全崩溃
- 所有 Rust 相关异常都会在日志中添加 `[rust]:` 前缀标记

## 使用说明（按角色分类）

### 1. 最终用户（游戏玩家）

只需简单两步即可使用：

1. 将 [rustforforge-1.0.0.jar](file://D:\文档\开发文件夹\GitHub\Minrust\build\libs\rustforforge-1.0.0.jar) 文件放入 Minecraft 的 `mods` 文件夹
2. 获取对应平台的 Rust 动态库文件并放置在正确位置：
   - Windows: `rustforforge_native.dll`
   - Linux: `librustforforge_native.so` 
   - macOS: `librustforforge_native.dylib`

**注意**：由于这是实验性模组，建议在测试环境中使用，避免在重要存档中使用。

### 2. 模组开发者

如果您是模组开发者，想要在自己的模组中使用 Rust for Forge 提供的高性能功能：

#### 环境要求
- Java 17 开发环境
- Minecraft Forge 开发环境

#### 使用方法
```java
// 导入接口
import org.tab.minrust.IRustToolbox;
import org.tab.minrust.RustPanicException;

// 获取实例
IRustToolbox rust = IRustToolbox.getInstance();

// 检查 Rust 库是否已加载
if (rust.isRustLibraryLoaded()) {
    try {
        // 调用 Rust 函数
        int sum = rust.add(5, 3);
        String processed = rust.processString("Hello");
        rust.mightPanic(false);
    } catch (Exception e) {
        // 处理 Rust panic 异常
        System.err.println("[rust]: " + e.getMessage());
        // 注意：虽然 JVM 不会崩溃，但仍需处理异常
    }
} else {
    // 降级处理或提示用户安装 Rust 库
    System.out.println("Rust library not available, using fallback implementation");
}
```

**重要提醒**：
- 调用前必须使用 [isRustLibraryLoaded()](file://D:\文档\开发文件夹\GitHub\Minrust\src\main\java\org\tab\minrust\RustToolboxImpl.java#L37-L39) 检查库是否已加载
- 所有 Rust 相关异常都会在日志中添加 `[rust]:` 前缀标记
- 由于是实验性功能，请做好错误处理和降级方案

### 3. Rust 开发者（扩展功能）

如果您想扩展 Rust 功能，需要：

#### 环境要求
- Rust 2021 edition 或更高版本
- Cargo 包管理器

#### 扩展步骤
1. 在 `rust/src/lib.rs` 中添加新的函数，使用 `#[no_mangle] extern "C"` 导出：

```rust
/// 新功能示例函数
use std::panic;

#[no_mangle]
pub extern "C" fn Java_org_tab_minrust_RustToolboxImpl_yourFunction(
    mut env: jni::JNIEnv,
    _class: jni::objects::JClass,
    // 添加需要的参数
) {
    // 使用 catch_unwind 捕获可能的 panic
    let result = panic::catch_unwind(std::panic::AssertUnwindSafe(|| {
        // 实现功能
        // 注意：不要手动捕获 panic，让框架处理
    }));
    
    // 如果发生 panic，向 Java 抛出异常
    if let Err(panic_info) = result {
        let message = match panic_info.downcast_ref::<&str>() {
            Some(s) => format!("[rust]: {}", s),
            None => match panic_info.downcast_ref::<String>() {
                Some(s) => format!("[rust]: {}", s),
                None => "[rust]: Rust panic occurred".to_string(),
            }
        };
        
        // 向 Java 抛出异常
        let _ = env.throw_new("java/lang/RuntimeException", &message);
    }
}
```

2. 在 Java 侧的 [IRustToolbox.java](file:///D:/文档/开发文件夹/GitHub/Minrust/src/main/java/org/tab/minrust/IRustToolbox.java) 接口中添加对应方法声明
3. 在 [RustToolboxImpl.java](file:///D:/文档/开发文件夹/GitHub/Minrust/src/main/java/org/tab/minrust/RustToolboxImpl.java) 中添加 native 方法声明和实现

## 构建项目

### 构建 Java 部分
```bash
# Windows
.\gradlew build

# Linux/macOS
./gradlew build
```

### 构建 Rust 部分
```bash
cd rust
cargo build --release
```

生成的动态库文件位置：
- Windows: `target/release/rustforforge_native.dll`
- Linux: `target/release/librustforforge_native.so`
- macOS: `target/release/librustforforge_native.dylib`

## 错误处理机制

当 Rust 代码发生 panic 时：
1. 异常会被捕获并转换为 Java RuntimeException
2. 在日志中添加 `[rust]:` 前缀标记
3. 抛出异常供 Java 层处理

```java
// 示例错误处理
try {
    rust.someRustFunction();
} catch (Exception e) {
    // 明确标识为 Rust 引起的异常
    logger.error("[rust]: Exception in Rust code: " + e.getMessage());
    // 实施相应的恢复措施，而不用担心 JVM 崩溃
}
```

## 架构设计特点

1. **安装即用**：无需配置、无需扫描依赖
2. **高性能**：Rust 原生执行，延迟低
3. **双向调用**：Java 与 Rust 无缝交互
4. **开发者友好**：第三方开发者可直接依赖接口
5. **不修改原生环境**：Minecraft/Forge/Fabric Loader 保持原始状态
6. **崩溃可追踪**：Rust 导致的崩溃在报错日志中明确标记
7. **增强的稳定性**：Rust panic 不再导致 JVM 崩溃