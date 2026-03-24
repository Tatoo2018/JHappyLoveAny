# 🚀 JHappyLoveAny

[![Build](https://github.com/Tatoo2018/JHappyLoveAny/actions/workflows/deploy.yml/badge.svg?branch=main)](https://github.com/Tatoo2018/JHappyLoveAny/actions/workflows/deploy.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
![Version](https://img.shields.io/badge/version-0.0.1-blue)

[🇺🇸 English](#-english) | [🇯🇵 日本語](#-日本語) | [🇨🇳 简体中文](#-简体中文)

---

## 🇺🇸 English

**JHappyLoveAny** is a **Language Server Protocol (LSP)** based plugin for **Eclipse IDE** and **Visual Studio Code** that provides powerful auto-completion and navigation for keys defined in `.properties` and `.xml` directly within Java source code.

It eliminates the hassle of searching for key definitions manually, significantly boosting coding productivity.

### 🌟 Key Features

- **🧠 Intelligent Completion:** `Press Ctrl + Space` within string literals ("") to auto-complete predefined keys.
- **📍 Jump to Definition:** `Ctrl + Click` (or F3) on a key to navigate directly to its definition.
- **ℹ️ Hover Information:** Hover over a key to view its value and definition details.
- **⚡ Auto-Scan:** Automatically indexes all resource files in the background.
- **💻 Multi-IDE Support:** Fully compatible with both **Eclipse IDE** and **Visual Studio Code**.

### 📺 Watch the Tutorial on YouTube:

<a href="https://www.youtube.com/watch?v=TDD1fWFYpc4" style="width:500px;display:block;">
  <img src="https://img.youtube.com/vi/TDD1fWFYpc4/0.jpg" alt="Watch the video" style="width:400px; max-width:500px;">
</a>

[Click here to watch](https://www.youtube.com/watch?v=TDD1fWFYpc4)

### ✨ Screenshots

![Usage Screenshots1](https://github.com/user-attachments/assets/0925a84d-90c5-48f9-a09c-0a1db7f0038d)
![Usage Screenshots2](https://github.com/user-attachments/assets/8e6f3883-22ae-48fa-926e-de30af7ccbed)
![Usage Screenshots3](https://github.com/user-attachments/assets/bf85f45f-743c-455e-8fc0-6c34fa747b6e)
![Usage Screenshots4](https://github.com/user-attachments/assets/ec70db99-0472-4ae8-9d61-b5b79cd804d4)

### ⚙️ Configuration
To use this plugin, you need a `jhappyqueries.xml` file in your workspace.

#### **Visual Studio Code:**
1. Open the **Command Palette** (`Ctrl+Shift+P`).
2. Search and select: `JHappy: Create Configuration File`.
3. A template `jhappyqueries.xml` will be generated in your workspace root.

#### **Eclipse IDE:**
- You can create it via `Window` > `Preferences` > `JHappy XML Queries File` or Project Properties.

![screenshot7](https://github.com/user-attachments/assets/d3d95525-c712-4e82-9941-4ac56521761d)

### 🔗 Downloads & Update URLs
| IDE | Stable (main) | Development (develop) |
| :--- | :--- | :--- |
| **Eclipse** | [Update Site URL](https://tatoo2018.github.io/JHappyLoveAny/updatesite/main/) | [Update Site URL](https://tatoo2018.github.io/JHappyLoveAny/updatesite/develop/) |
| **VSCode** | [Download .vsix](https://tatoo2018.github.io/JHappyLoveAny/vscode/main/) | [Download .vsix](https://tatoo2018.github.io/JHappyLoveAny/vscode/develop/) |

---

## 🇯🇵 日本語

**JHappyLoveAny** は、**Eclipse IDE** および **Visual Studio Code** 上でプロパティファイル（`.properties`）や XML ファイルで定義されたキーを、Java ソースコード内で強力に補完・ナビゲートするための **Language Server Protocol (LSP)** ベースのプラグインです。

### 🌟 主な機能

- **🧠 インテリジェントな補完:** 文字列リテラル (`""`) 内で `Ctrl + Space` を押すと、定義済みのキーを自動補完します。
- **📍 定義へのジャンプ:** キーの上で `Ctrl + Click` (または `F3`) を押すと、定義場所へ直接ジャンプします。
- **ℹ️ ホバー情報:** キーの上にカーソルを合わせると、その値や定義の詳細をポップアップ表示します。
- **⚡ 自動スキャン:** バックグラウンドで全リソースファイルを自動的にインデックス化します。
- **💻 マルチ IDE 対応:** **Eclipse IDE** と **VSCode** の両方をサポート。

### ⚙️ 設定方法（jhappyqueries.xml の作成）

#### **Visual Studio Code:**
1. **コマンドパレット** (`Ctrl+Shift+P`) を開きます。
2. `JHappy: Create Configuration File` を実行します。
3. ワークスペースのルートに `jhappyqueries.xml` が生成されます。

#### **Eclipse IDE:**
- `Window` > `Preferences` > `JHappy XML Queries File` またはプロジェクトのプロパティから作成できます。

### 🔗 インストール・ダウンロード URL
| IDE | 安定版 (main) | 開発版 (develop) |
| :--- | :--- | :--- |
| **Eclipse** | [更新サイト URL](https://tatoo2018.github.io/JHappyLoveAny/updatesite/main/) | [更新サイト URL](https://tatoo2018.github.io/JHappyLoveAny/updatesite/develop/) |
| **VSCode** | [VSIX ダウンロード](https://tatoo2018.github.io/JHappyLoveAny/vscode/main/) | [VSIX ダウンロード](https://tatoo2018.github.io/JHappyLoveAny/vscode/develop/) |

---

## 🇨🇳 简体中文

**JHappyLoveAny** 是一款基于 **Language Server Protocol (LSP)** 的插件，支持 **Eclipse IDE** 和 **Visual Studio Code**。它旨在为 Java 源代码中引用的 `.properties` 和 XML 文件键值提供强大的自动补全与导航功能。

### 🌟 主要功能

- **🧠 智能补全:** 在字符串字面量 (`""`) 中按下 `Ctrl + Space`，即可自动补全预定义的键。
- **📍 跳转到定义:** 在键上按下 `Ctrl + 单击` (或 `F3`)，可直接跳转到定义位置。
- **ℹ️ 悬停提示:** 将鼠标悬停在键上，会以弹窗形式显示其值和定义的详细信息。
- **⚡ 自动扫描:** 在后台自动索引项目中的所有资源文件。
- **💻 多 IDE 支持:** 完美支持 **Eclipse IDE** 和 **VS Code**。

### ⚙️ 配置方法（创建 jhappyqueries.xml）

#### **Visual Studio Code:**
1. 打开 **命令面板** (`Ctrl+Shift+P`)。
2. 选择 `JHappy: Create Configuration File`。
3. 工作区根目录中将生成 `jhappyqueries.xml` 模板。

#### **Eclipse IDE:**
- 通过 `Window` > `Preferences` > `JHappy XML Queries File` 或项目属性进行创建。

---

### 🏗️ Project Structure

This repository uses a multi-module configuration powered by **Maven / Tycho**.

| Module Name | Role |
| :--- | :--- |
| `com.jhappy.jhappyloveany` | **LSP Server Core** (Pure Java / LSP4J) |
| `com.jhappy.jhappyloveany.client` | **Eclipse Plugin** (LSP4E Client) |
| `com.jhappy.jhappyloveany.client.vscode` | **VS Code Extension** (Client) |
| `com.jhappy.jhappyloveany.updatesite` | P2 Repository (Update Site) Generation |
| `com.jhappy.jhappyloveany.test` | UI and Integration Tests (SWTBot / JUnit 5) |

### 🛠️ Build Instructions

```bash
# Build all modules and run tests
mvn clean verify
