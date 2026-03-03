# 🚀 JHappyLoveAny

[![Build](https://github.com/Tatoo2018/JHappyLoveAny/actions/workflows/deploy.yml/badge.svg?branch=main)](https://github.com/Tatoo2018/JHappyLoveAny/actions/workflows/deploy.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![Version](https://img.shields.io/badge/version-0.0.1-blue)

[🇺🇸 English](#-english) | [🇯🇵 日本語](#-日本語) | [🇨🇳 简体中文](#-简体中文)

---

## 🇺🇸 English

**JHappyLoveAny** is a **Language Server Protocol (LSP)** based plugin for the Eclipse IDE that provides powerful auto-completion and navigation for keys defined in `.properties` and XML files directly within Java source code.

It eliminates the hassle of searching for key definitions manually, significantly boosting coding productivity.

### 🌟 Key Features

-- ** 🧠 Intelligent ** Completion: Press Ctrl + Space within string literals ("") to auto-complete predefined keys.
-- ** 📍 Jump to Definition: ** Ctrl + Click (or F3) on a key to navigate directly to its definition in .properties or XML files.
-- ** ℹ️ Hover Information: ** Hover over a key to view a popup with its value and definition details.
-- ** ⚡ Auto-Scan: ** Automatically indexes all resource files in the background, ensuring high-speed performance.
-- ** 🔧 Customizable Targets: ** Specify target files using Regular Expressions to flexibly support custom directory structures. (e.g., src/.*\.xml$)
-- ** 📝 XPath Support: ** Define target XML attributes using XPath, allowing flexibility for custom XML schemas. (e.g., //@name)
-- ** 🔀 Flexible Matching: ** Toggle between Prefix (starts with) and Contains (partial match) modes for auto-completion.

![screenshot5](https://github.com/user-attachments/assets/0925a84d-90c5-48f9-a09c-0a1db7f0038d)

![screenshot4](https://github.com/user-attachments/assets/8e6f3883-22ae-48fa-926e-de30af7ccbed)

![screenshot2](https://github.com/user-attachments/assets/bf85f45f-743c-455e-8fc0-6c34fa747b6e)

![screenshot3](https://github.com/user-attachments/assets/2e3d34ba-cd96-47cc-9f41-6f891b5164ba)

![screenshot1](https://github.com/user-attachments/assets/55e33219-a7c3-44b6-b67e-1a195e20b331)

![screenshot6](https://github.com/user-attachments/assets/7970ee69-9442-4319-820d-53666e2f5f0e)

### 🔗 Update Site URLs
| Environment | Branch | Eclipse Update Site URL |
| :--- | :--- | :--- |
| **Stable** | `main` | `https://tatoo2018.github.io/JHappyLoveAny/updatesite/main/` |
| **Development** | `develop` | `https://tatoo2018.github.io/JHappyLoveAny/updatesite/develop/` |

### 🏗️ Project Structure

This repository uses a multi-module configuration powered by **Maven / Tycho**.

| Module Name | Role |
| :--- | :--- |
| `com.jhappy.jhappyloveany` | **LSP Server Core** (Pure Java / LSP4J) |
| `com.jhappy.jhappyloveany.client` | **Eclipse Plugin** (LSP4E Client) |
| `com.jhappy.jhappyloveany.feature` | Eclipse Feature Project |
| `com.jhappy.jhappyloveany.updatesite` | P2 Repository (Update Site) Generation |
| `com.jhappy.jhappyloveany.test` | UI and Integration Tests (SWTBot / JUnit 5) |

### 🛠️ Build Instructions

**Java 21** and **Maven** are required for building.

```bash
# Build all modules and run tests
mvn clean verify
```

## 🇯🇵 日本語

**JHappyLoveAny** は、Eclipse IDE 上でプロパティファイル（`.properties`）や XML ファイルで定義されたキーを、Java ソースコード内で強力に補完・ナビゲートするための **Language Server Protocol (LSP)** ベースのプラグインです。

開発者がキーの定義場所を探し回る手間を省き、コーディングの生産性を大幅に向上させます。

### 🌟 主な機能

- **🧠 インテリジェントな補完**: 文字列リテラル内 (`""`) で `Ctrl + Space` を押すと、定義済みのキーを自動補完します。
- **📍 定義へのジャンプ**: キーの上で `Ctrl + クリック` (または `F3`) を押すと、プロパティファイルや XML ファイル内の定義場所へ直接ジャンプします。
- **ℹ️ ホバー情報**: キーの上にマウスを置くと、その値や定義場所の詳細をポップアップ表示します。
- **⚡ 自動スキャン**: バックグラウンドでプロジェクト内のすべてのリソースファイルを自動的にインデックス化し、高速な動作を実現します。

### 🔗 Update Site URLs
| Environment | Branch | Eclipse Update Site URL |
| :--- | :--- | :--- |
| **Stable** | `main` | `https://tatoo2018.github.io/JHappyLoveAny/updatesite/main/` |
| **Development** | `develop` | `https://tatoo2018.github.io/JHappyLoveAny/updatesite/develop/` |

### 🏗️ プロジェクト構造

本リポジトリは **Maven / Tycho** を利用したマルチモジュール構成になっています。

| モジュール名 | 役割 |
| :--- | :--- |
| `com.jhappy.jhappyloveany` | **LSP サーバー本体** (Pure Java / LSP4J) |
| `com.jhappy.jhappyloveany.client` | **Eclipse プラグイン** (LSP4E クライアント) |
| `com.jhappy.jhappyloveany.feature` | Eclipse Feature プロジェクト |
| `com.jhappy.jhappyloveany.updatesite` | P2 リポジトリ (更新サイト) 生成用 |
| `com.jhappy.jhappyloveany.test` | UI および統合テスト (SWTBot / JUnit 5) |

### 🛠️ ビルド方法

ビルドには **Java 21** と **Maven** が必要です。

```bash
# 全モジュールのビルドとテストの実行
mvn clean verify
```


## 🇨🇳 简体中文

**JHappyLoveAny** 是一款基于 **Language Server Protocol (LSP)** 的 Eclipse IDE 插件，旨在为 Java 源代码中引用的 `.properties` 和 XML 文件键值提供强大的自动补全与导航功能。

它免去了开发者手动查找键值定义的麻烦，从而显著提升编码生产力。

### 🌟 主要功能

- **🧠 智能补全**: 在字符串字面量 (`""`) 中按下 `Ctrl + Space`，即可自动补全已定义的键。
- **📍 跳转定义**: 在键名上按下 `Ctrl + 单击` (或 `F3`)，可直接跳转到属性文件或 XML 文件中的对应定义行。
- **ℹ️ 悬停信息**: 将光标悬停在键名上，弹出窗口将显示其值和定义详情。
- **⚡ 自动扫描**: 在后台自动索引项目中的所有资源文件，实现高速运行。

### 🔗 Update Site URLs
| Environment | Branch | Eclipse Update Site URL |
| :--- | :--- | :--- |
| **Stable** | `main` | `https://tatoo2018.github.io/JHappyLoveAny/updatesite/main/` |
| **Development** | `develop` | `https://tatoo2018.github.io/JHappyLoveAny/updatesite/develop/` |

### 🏗️ 项目结构

本仓库采用基于 **Maven / Tycho** 的多模块结构。

| 模块名称 | 角色 |
| :--- | :--- |
| `com.jhappy.jhappyloveany` | **LSP 服务端核心** (Pure Java / LSP4J) |
| `com.jhappy.jhappyloveany.client` | **Eclipse 插件** (LSP4E 客户端) |
| `com.jhappy.jhappyloveany.feature` | Eclipse Feature 项目 |
| `com.jhappy.jhappyloveany.updatesite` | P2 仓库 (Update Site) 生成 |
| `com.jhappy.jhappyloveany.test` | UI 及集成测试 (SWTBot / JUnit 5) |

### 🛠️ 构建方法

构建需要 **Java 21** 和 **Maven**。

```bash
# 构建所有模块并运行测试
mvn clean verify
```
