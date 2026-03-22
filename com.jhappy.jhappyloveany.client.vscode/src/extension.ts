import * as path from "path";
import * as vscode from 'vscode';
import {
  LanguageClient,
  LanguageClientOptions,
  ServerOptions,
} from "vscode-languageclient/node";

import * as fs from 'fs';


export function activate(context: vscode.ExtensionContext) {
  const serverModule = context.asAbsolutePath(
    path.join("server", "com.jhappy.jhappyloveany-jar-with-dependencies.jar"),
  );

  const serverOptions: ServerOptions = {
    command: "java",
    args: ["-jar", serverModule],
    options: {
      env: process.env,
    },
  };

  const clientOptions: LanguageClientOptions = {
    documentSelector: [
      { scheme: "file", language: "java" },
      { scheme: "file", language: "properties" },
      { scheme: "file", language: "xml" },
    ],
  };

  const client = new LanguageClient(
    "jhappyLSP",
    "JHappy Language Server",
    serverOptions,
    clientOptions,
  );
  client.start();


  let disposable = vscode.commands.registerCommand('jhappy-lsp-client.createConfig', async () => {
        const workspaceFolders = vscode.workspace.workspaceFolders;
        if (!workspaceFolders) {
            vscode.window.showErrorMessage('ワークスペースが開かれていません。');
            return;
        }

        const rootPath = workspaceFolders[0].uri.fsPath;
        const configPath = path.join(rootPath, 'jhappyqueries.xml');

        if (fs.existsSync(configPath)) {
            vscode.window.showInformationMessage('jhappyqueries.xml は既に存在します。');
            return;
        }

        // 1. 拡張機能内のリソースファイルへのパスを取得
        const templatePath = context.asAbsolutePath(path.join('resources', 'jhappyqueries.xml'));

        try {
            // 2. ファイルを読み込んでコピー（または書き込み）
            if (!fs.existsSync(templatePath)) {
                vscode.window.showErrorMessage('テンプレートファイルが見つかりません: ' + templatePath);
                return;
            }

            const content = fs.readFileSync(templatePath, 'utf8');
            fs.writeFileSync(configPath, content, 'utf8');

            // 3. 作成したファイルを開く
            const doc = await vscode.workspace.openTextDocument(configPath);
            await vscode.window.showTextDocument(doc);
            vscode.window.showInformationMessage('テンプレートから jhappyqueries.xml を作成しました。');
            
        } catch (err) {
            vscode.window.showErrorMessage('ファイルのコピーに失敗しました: ' + err);
        }
    });

    context.subscriptions.push(disposable);

}

