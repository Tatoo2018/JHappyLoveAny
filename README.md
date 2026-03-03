# 🚀 JHappyLoveAny

![Build Status](https://github.com/Tatoo2018/JHappyLoveAny/actions/workflows/maven.yml/badge.svg)
![License](https://img.shields.io/github/license/Tatoo2018/JHappyLoveAny)
![Version](https://img.shields.io/badge/version-1.0.0-blue)

**JHappyLoveAny** は、Eclipse IDE 上でプロパティファイル（`.properties`）や XML ファイルで定義されたキーを、Java ソースコード内で強力に補完・ナビゲートするための **Language Server Protocol (LSP)** ベースのプラグインです。

開発者がキーの定義場所を探し回る手間を省き、コーディングの生産性を向上させます。

## 🌟 主な機能

- **🧠 インテリジェントな補完**: 文字列リテラル内 (`""`) で `Ctrl + Space` を押すと、定義済みのキーを自動補完します。
- **jump 定義へのジャンプ**: `Ctrl + クリック` (または `F3`) で、キーが定義されているプロパティファイルや XML の該当行へ直接ジャンプします。
- **ℹ️ ホバー情報**: キーの上にカーソルを置くと、その値や定義場所の詳細をポップアップ表示します。
- **⚡ 自動スキャン**: プロジェクト内の全リソースファイルをバックグラウンドで高速にインデックス化します。

## 🏗️ プロジェクト構造

本リポジトリは **Maven / Tycho** を利用したマルチモジュール構成になっています。

| モジュール名 | 役割 |
| :--- | :--- |
| `com.jhappy.jhappyloveany` | **LSP サーバー本体** (Pure Java / LSP4J) |
| `com.jhappy.jhappyloveany.client` | **Eclipse プラグイン** (LSP4E クライアント) |
| `com.jhappy.jhappyloveany.feature` | Eclipse Feature プロジェクト |
| `com.jhappy.jhappyloveany.updatesite` | P2 リポジトリ (更新サイト) 生成用 |
| `com.jhappy.jhappyloveany.test` | UI および統合テスト (SWTBot / JUnit 5) |

## 🛠️ ビルド方法


```bash
# 全モジュールのビルドとテストの実行
mvn clean verify
