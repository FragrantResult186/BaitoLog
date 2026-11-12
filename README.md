# BaitoLog

アルバイトをする留学生の勤務時間（連続する7日間の上限）を管理する Web アプリ。卒業制作 Aチーム。

授業の見本（得点管理システム `javaSystemDev_login`）と同じ作り方にしている。

| 項目 | 内容 |
|---|---|
| 開発環境 | Eclipse（Pleiades 2023-12） |
| 言語 | Java 21 |
| サーバー | Tomcat 10.1（Pleiades の「Tomcat10 (Java21)」） |
| 作り方 | Servlet / JSP（FrontController ＋ Action ＋ DAO ＋ Bean） |
| DB | Neon |

---

## Neonへの接続設定

アプリはTomcatのJNDIリソース `jdbc/baitolog` を使います。Neonの接続情報は共有リポジトリに登録せず、各自の実行環境に設定してください。

NeonのJDBC URLは次の形式です。実際のホスト名とデータベース名はNeon Consoleに表示された値を使い、TLS用の `sslmode=require` を付けてください。

```text
jdbc:postgresql://<Neonのホスト>/<データベース名>?sslmode=require
```

### Eclipse / Tomcatで起動する

1. Eclipseの「サーバー」ビューで `Tomcat10 (Java21)` をダブルクリックし、サーバー構成を確認します。
2. ワークスペースの `Servers/Tomcat10_Java21-config/catalina.properties` を開き、末尾に次を追加します。

   ```properties
   db.url=jdbc:postgresql://<Neonのホスト>/<データベース名>?sslmode=require
   db.user=<Neonのデータベースユーザー>
   db.password=<そのユーザーのパスワード>
   ```

3. この `catalina.properties` は各自のEclipseワークスペース内だけに置き、Gitへ追加しません。EclipseでTomcatを再起動します。

秘密情報をVM引数に書かないでください。Tomcatの起動ログにVM引数が記録されます。

### Renderで起動する

RenderのWeb ServiceのEnvironmentに次の3つを設定します。

- `DB_URL`: Neon ConsoleのJDBC URL
- `DB_USER`: Neonのデータベースユーザー
- `DB_PASSWORD`: そのユーザーのパスワード

DockerfileはTomcatの環境変数プロパティソースを有効にしています。Render側で `JAVA_OPTS` を上書きする場合は、Dockerfileにある `PROPERTY_SOURCE` の設定を残してください。

### Neonのスキーマとチーム共有

新しいNeonデータベースには、最初に `sql/create_tables.sql`、続けて `sql/sample_data.sql` を一度だけ実行します。Neon Consoleを使うメンバーは、Neonプロジェクトへ個別に招待してもらってください。アプリ用には所有者アカウントを共有せず、必要な権限だけを持つDBユーザーを管理者が用意します。接続情報は安全な方法で各自に共有し、各自のEclipse設定またはRenderの環境変数へ登録してください。パスワード、Neonの完全な接続文字列、アプリのログインパスワードはソース・README・コミットに含めません。

