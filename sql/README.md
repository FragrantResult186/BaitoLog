# SQL（テーブル作成・サンプルデータ）

解説書「設計書からプログラムの書き起こし」と同じ手順で、テーブル定義書から CREATE 文を作ってここに置く。

| ファイル | 中身 |
|---|---|
| `create_tables.sql` | 全テーブルの CREATE 文（テーブル定義書から作る） |
| `sample_data.sql` | 動作確認用のサンプルデータ（INSERT 文） |

## H2 コンソールで実行する
1. **Tomcat を止めた状態で**、`src/main/webapp/WEB-INF/lib/h2-2.1.214.jar` をダブルクリックする（またはコマンドで `java -jar h2-2.1.214.jar`）
2. ブラウザに H2 コンソールが開くので、次のように入力して「接続」
   - JDBC URL：`jdbc:h2:~/baitolog;AUTO_SERVER=TRUE`
   - ユーザー名：`sa`　パスワード：空
3. SQL を貼り付けて「実行」

## ルール
- **テーブルを変えたら、テーブル定義書とこのフォルダの SQL を両方直す**
- SQL を変えたら、チームに「◯◯テーブルを変えたので create_tables.sql を流し直して」と連絡する（各自のPCのDBは自動では変わらない）
- DBを作り直したいときは、`DROP TABLE` してから流し直す。またはホームフォルダの `baitolog.mv.db` を消す
