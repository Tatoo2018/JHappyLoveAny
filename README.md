# 🚀 JHappyLoveAny

[![Build](https://github.com/Tatoo2018/JHappyLoveAny/actions/workflows/deploy.yml/badge.svg?branch=main)](https://github.com/Tatoo2018/JHappyLoveAny/actions/workflows/deploy.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![Version](https://img.shields.io/badge/version-0.0.1-blue)

[🇺🇸 English](#-english) | [🇯🇵 日本語](#-日本語) | [🇨🇳 简体中文](#-简体中文)

---

## 🇺🇸 English

**JHappyLoveAny** is a **Language Server Protocol (LSP)** based plugin for the Eclipse IDE that provides powerful auto-completion and navigation for keys defined in `.properties` and `.xml` directly within Java source code.

It eliminates the hassle of searching for key definitions manually, significantly boosting coding productivity.

### 🌟 Key Features

- **🧠 Intelligent** Completion: `Press Ctrl + Space` within string literals ("") to auto-complete predefined keys.
- **📍 Jump to Definition:** `Ctrl + Click` (or F3) on a key to navigate directly to its definition in .properties or XML files.
- **ℹ️ Hover Information:** Hover over a key to view a popup with its value and definition details.
- **⚡ Auto-Scan:** Automatically indexes all resource files in the background, ensuring high-speed performance.
- **🔧 Customizable Targets:** Specify target files using `Regular Expressions` to flexibly support custom directory structures. (e.g., `src/.*\.xml$`)
- **📝 XPath Support:** Define target XML attributes using `XPath`, allowing flexibility for custom XML schemas. (e.g., `//@name`)
- **🔀 Flexible Matching:** Toggle between Prefix (`starts with`) and Contains (`partial match`) modes for auto-completion.

<a href="https://www.youtube.com/watch?v=k_hTIWZvwTE" style="width:400px;display:block;">
  <img src="https://img.youtube.com/vi/k_hTIWZvwTE/0.jpg" alt="Watch the video" style="width:400px; max-width:400px;">
</a>

![Usage Screenshots1](https://github.com/user-attachments/assets/0925a84d-90c5-48f9-a09c-0a1db7f0038d)

![Usage Screenshots2](https://github.com/user-attachments/assets/8e6f3883-22ae-48fa-926e-de30af7ccbed)

![Usage Screenshots3](https://github.com/user-attachments/assets/bf85f45f-743c-455e-8fc0-6c34fa747b6e)

![Usage Screenshots4](https://github.com/user-attachments/assets/ec70db99-0472-4ae8-9d61-b5b79cd804d4)

![Usage Screenshots5](https://github.com/user-attachments/assets/55e33219-a7c3-44b6-b67e-1a195e20b331)

![Usage Screenshots6](https://github.com/user-attachments/assets/7970ee69-9442-4319-820d-53666e2f5f0e)

## ⚙️ Configuration
You can create the configuration file via the project properties.:
`Window` > `Preferences` > `JHappy XML Queries File`

![screenshot7](https://github.com/user-attachments/assets/d3d95525-c712-4e82-9941-4ac56521761d)

## 📚 Help Documentation
You can find the user manual within Eclipse:
`Help` > `Help Contents` > `JHappyLoveAny Plugin User Guide`

![screenshotimage8](https://github.com/user-attachments/assets/6cd9e9d3-bc4b-4a71-b552-9f671c36e7b5)


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

```bash
# Build all modules and run tests
mvn clean verify
```

## 🇯🇵 日本語

**JHappyLoveAny** は、Eclipse IDE 上で`プロパティファイル` や `XML ファイル`で定義されたキーを、Java ソースコード内で強力に補完・ナビゲートするための **Language Server Protocol (LSP)** ベースのプラグインです。

開発者がキーの定義場所を探し回る手間を省き、コーディングの生産性を大幅に向上させます。

### 🌟 主な機能

- **🧠 インテリジェントな補完:** 文字列リテラル (`""`) 内で `Ctrl + Space` を押すと、定義済みのキーを自動補完します。
- **📍 定義へのジャンプ:** キーの上で `Ctrl + Click` (または `F3`) を押すと、`.properties` や XML ファイル内の定義場所へ直接ジャンプします。
- **ℹ️ ホバー情報:** キーの上にカーソルを合わせると、その値や定義の詳細をポップアップ表示します。
- **⚡ 自動スキャン:** バックグラウンドでプロジェクト内の全リソースファイルを自動的にインデックス化し、高速な動作を実現します。
- **🔧 解析対象のカスタマイズ:** `正規表現` を使用して解析対象ファイルを指定でき、独自のディレクトリ構成にも柔軟に対応します。(例: `src/.*\.xml$`)
- **📝 XPath サポート:** `XPath` を使用して解析対象の XML 属性を指定でき、独自の XML 定義 (スキーマ) に柔軟に対応します。(例: `//@name`)
- **🔀 柔軟なマッチング:** 自動補完の挙動を、前方一致 (`prefix`) か 部分一致 (`contains`) かで切り替えることができます。

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

```bash
# 全モジュールのビルドとテストの実行
mvn clean verify
```


## 🇨🇳 简体中文

**JHappyLoveAny** 是一款基于 **Language Server Protocol (LSP)** 的 Eclipse IDE 插件，旨在为 Java 源代码中引用的 `.properties` 和 XML 文件键值提供强大的自动补全与导航功能。

它免去了开发者手动查找键值定义的麻烦，从而显著提升编码生产力。

### 🌟 主要功能

- **🧠 智能补全:** 在字符串字面量 (`""`) 中按下 `Ctrl + Space`，即可自动补全预定义的键。
- **📍 跳转到定义:** 在键上按下 `Ctrl + 单击` (或 `F3`)，可直接跳转到 `.properties` 或 XML 文件中的定义位置。
- **ℹ️ 悬停提示:** 将鼠标悬停在键上，会以弹窗形式显示其值和定义的详细信息。
- **⚡ 自动扫描:** 在后台自动索引项目中的所有资源文件，实现高速运行。
- **🔧 自定义解析目标:** 支持使用 `正则表达式` 指定解析目标文件，灵活适配自定义的目录结构。(例如: `src/.*\.xml$`)
- **📝 XPath 支持:** 支持使用 `XPath` 指定解析目标 XML 属性，灵活适配自定义的 XML 定义 (Schema)。(例如: `//@name`)
- **🔀 灵活匹配:** 可以在前缀匹配 (`prefix`) 或包含匹配 (`contains`) 之间切换自动补全模式。

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

```bash
# 构建所有模块并运行测试
mvn clean verify
```
