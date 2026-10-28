# BaitoLog

アルバイトをする留学生の勤務時間（連続する7日間の上限）と収入を管理する Web アプリ。卒業制作 Aチーム。

授業の見本（得点管理システム `javaSystemDev_login`）と同じ作り方にしている。

| 項目 | 内容 |
|---|---|
| 開発環境 | Eclipse（Pleiades 2023-12） |
| 言語 | Java 21 |
| サーバー | Tomcat 10.1（Pleiades の「Tomcat10 (Java21)」） |
| 作り方 | Servlet / JSP（FrontController ＋ Action ＋ DAO ＋ Bean） |
| DB | H2 2.1.214 |

---

## 1. はじめにやること（全員・1回だけ）

### 1-1. Eclipse に取り込む
1. **ファイル > インポート > Git > Git からプロジェクト > URI のクローン** で GitHub の URL を入れて、最後に「既存プロジェクトのインポート」で BaitoLog を選ぶ
   - すでにクローン済みの人：**ファイル > インポート > 一般 > 既存プロジェクトをワークスペースへ** でクローンしたフォルダを選ぶ
   - **前の Spring Boot 版を取り込んでいた人**：先にその BaitoLog を右クリック > 削除（「ディスク上からプロジェクト・コンテンツを削除」には**チェックしない**）→ `git pull` → 上の手順で取り込み直す
2. エラーが出ていないか確認する。「Tomcat10 (Java21) がない」と出たら：**ウィンドウ > 設定 > サーバー > ランタイム環境** に「Tomcat10 (Java21)」があるか確認（Pleiades なら最初から入っている）

### 1-2. サーバーに追加する
1. 下の **サーバー** タブで、Tomcat10 (Java21) のサーバーを作る（無ければ「新規 > サーバー > Tomcat v10.1」）
2. サーバーを右クリック > **追加および除去** で BaitoLog を右へ移す

---

## 2. 起動のしかた

1. サーバー タブで Tomcat10 (Java21) を選び **▶（起動）**
2. ブラウザで **http://localhost:8080/BaitoLog/** を開く
3. 「起動確認」「データベース接続：OK」と出れば準備完了

- DB は最初にアクセスしたときに自動で作られる（場所：`C:\Users\自分\baitolog.mv.db`）。**H2 を先に起動しておく必要はない**
- 止めるときはサーバー タブの **■（停止）**

---

## 3. ファイルの置き場所（見本と同じ）

```
src/main/java/
  tool/      Action（親クラス）、FrontController、EncodingFilter   ← 変更しない
  dao/       Dao（親クラス）、〇〇Dao                            ← SQL を書くクラス
  bean/      User（親クラス）、〇〇（データの入れ物）
  baitolog/           ログイン前の画面のアクション（LoginAction など）
  baitolog/student/   学生の画面のアクション
  baitolog/teacher/   教職員の画面のアクション
src/main/webapp/
  common/    base.jsp（共通テンプレート）、header / footer / navigation
  baitolog/           ログイン前の画面の JSP
  baitolog/student/   学生の画面の JSP
  baitolog/teacher/   教職員の画面の JSP
  css/style.css
  META-INF/context.xml  DBの接続設定
  WEB-INF/lib/          H2・JSTL の jar
sql/         テーブル作成・サンプルデータの SQL
```

**アクションと JSP は同じフォルダ名にそろえる。** 例：`baitolog/student/ShiftEdit.action` を開くと `baitolog.student.ShiftEditAction` が動き、`baitolog/student/shift_edit.jsp` にフォワードする。

アクションの中身は見本と同じ **手順1〜7** のコメントに沿って書く。
1. ローカル変数の宣言
2. リクエストパラメータの取得
3. DBからデータ取得
4. ビジネスロジック
5. DBへデータ保存
6. レスポンス値をセット
7. JSPへフォワード

---

## 4. チームのルール

1. **`tool` パッケージ・`common/base.jsp`・`context.xml` は勝手に変えない**（全員の画面に影響する。変えたいときはリーダーに相談）
2. **自分の担当のフォルダ以外のファイルを触るときは、先に一言伝える**
3. **テーブルを変えたら、テーブル定義書と `sql/` の SQL を両方直して、チームに連絡する**
4. **コミットする前に、自分のPCで起動して画面が動くことを確認する**
5. ソースの一括整形（Ctrl+Shift+F）は自分が編集したファイルだけにする

## 5. Git の進め方

- `main` に直接プッシュしない。**作業ごとにブランチを作る**（Eclipse：プロジェクトを右クリック > チーム > 切り替え > 新規ブランチ）
  - 例：`feature/login`、`feature/shift`、`feature/report`
- 作業の前に **プル** して最新にする（チーム > プル）
- 終わったら **コミット → プッシュ** して、GitHub で **プルリクエスト** を作る。**ほかのメンバー1人が確認してからマージ**する
- コミットメッセージは日本語でよい。何をしたか1行で（例：「シフト登録画面の入力チェックを追加」）
