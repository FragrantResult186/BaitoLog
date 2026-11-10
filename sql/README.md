# SQL（テーブル作成・サンプルデータ）

解説書「設計書からプログラムの書き起こし」と同じ手順で、テーブル定義書から CREATE 文を作ってここに置く。

| ファイル | 中身 |
|---|---|
| `create_tables.sql` | 全テーブルの CREATE 文（テーブル定義書から作る） |
| `sample_data.sql` | 動作確認用のサンプルデータ（INSERT 文） |

## PostgreSQL の準備（各自のPCで1回だけ）
DB は PostgreSQL。各自のPCに入れて使う（接続先：`localhost:5432`、DB名：`baitolog`）。

1. PostgreSQL をインストールする（インストーラの管理者 `postgres` のパスワードは忘れないようにする）
2. **pgAdmin**（インストーラに付いてくる）を開き、サーバーに接続して「クエリツール」を開く。または `psql -U postgres`
3. 次の2つを、1つずつ実行する（開発用のユーザーとDBを作る。全員同じにして、context.xml を共通にする）
```sql
CREATE USER baitolog WITH PASSWORD 'baitolog';
```
```sql
CREATE DATABASE baitolog OWNER baitolog ENCODING 'UTF8' TEMPLATE template0;
```
4. 作った `baitolog` データベースに、ユーザー `baitolog`（パスワード `baitolog`）で接続し直す
5. `create_tables.sql` の中身を貼り付けて実行し、そのあと `sample_data.sql` を実行する

（pgAdmin では、左の一覧で `baitolog` データベースを右クリック → 「クエリツール」を選ぶと、そのDBにつながった状態で開く。接続のユーザーは `postgres` のままでもよいが、そのときは作ったテーブルの持ち主が `postgres` になるので、アプリ側（ユーザー `baitolog`）からは権限エラーになる場合がある。迷ったらユーザー `baitolog` で接続する）

## ルール
- **テーブルを変えたら、テーブル定義書とこのフォルダの SQL を両方直す**
- SQL を変えたら、チームに「◯◯テーブルを変えたので create_tables.sql を流し直して」と連絡する（各自のPCのDBは自動では変わらない）
- DBを作り直したいときは、次の2行を実行してから、`create_tables.sql` → `sample_data.sql` の順に流し直す
```sql
DROP SCHEMA public CASCADE;
```
```sql
CREATE SCHEMA public;
```

## ルール
- **テーブルを変えたら、テーブル定義書とこのフォルダの SQL を両方直す**
- SQL を変えたら、チームに「◯◯テーブルを変えたので create_tables.sql を流し直して」と連絡する（各自のPCのDBは自動では変わらない）
- DBを作り直したいときは、`DROP TABLE` してから流し直す。またはホームフォルダの `baitolog.mv.db` を消す

## テスト用アカウント（sample_data.sql）
| ログインID | パスワード | 内容 |
|---|---|---|
| 2502027 | password | 学生（パスワード登録済み。勤務先2件） |
| 2502028 | 20050312 | 学生（初回ログイン。新パスワード登録へ進む） |
| teacher01 | password | 教職員 |

`create_tables.sql` → `sample_data.sql` の順に、空のDBへ1回だけ流す。

※ 小数の列は PostgreSQL に合わせて `DOUBLE PRECISION` にしてある（`DOUBLE` はPostgreSQLにない）。

## 既存のDBに生年月日の列を足す（学生アカウント一括登録のため）
STUDENT テーブルに BIRTHDAY 列を追加した。すでにテーブルを作っている人は、次を1回実行する。
```sql
ALTER TABLE STUDENT ADD COLUMN IF NOT EXISTS BIRTHDAY DATE;
```
（流し直す人は、`create_tables.sql` → `sample_data.sql` の順でよい）
