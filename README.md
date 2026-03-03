# 🚀 JHappyLoveAny

[![Build](https://github.com/Tatoo2018/JHappyLoveAny/actions/workflows/deploy.yml/badge.svg?branch=main)](https://github.com/Tatoo2018/JHappyLoveAny/actions/workflows/deploy.yml)
![License](https://img.shields.io/github/license/Tatoo2018/JHappyLoveAny)
![Version](https://img.shields.io/badge/version-0.0.1-blue)

[🇺🇸 English](#-english) | [🇯🇵 日本語](#-日本語) | [🇨🇳 简体中文](#-简体中文)

---

## 🇺🇸 English

**JHappyLoveAny** is a **Language Server Protocol (LSP)** based plugin for the Eclipse IDE that provides powerful auto-completion and navigation for keys defined in `.properties` and XML files directly within Java source code.

It eliminates the hassle of searching for key definitions manually, significantly boosting coding productivity.

### 🌟 Key Features

- **🧠 Intelligent Completion**: Press `Ctrl + Space` inside string literals (`""`) to auto-complete defined keys.
- **Run Jump to Definition**: `Ctrl + Click` (or `F3`) on a key to jump directly to its definition in the properties or XML file.
- **ℹ️ Hover Information**: Hover over a key to view its value and definition details in a popup.
- **⚡ Auto Scan**: Automatically indexes all resource files in the project in the background for high performance.

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
