# BaitoLog

アルバイトをする留学生の勤務時間（連続する7日間の上限）と収入を管理する Web アプリ。卒業制作 Aチーム。

| 項目 | 内容 |
|---|---|
| 言語 | Java 21 |
| フレームワーク | Spring Boot 4.1（Tomcat内蔵。**Tomcat の設定・インストールは不要**） |
| 画面 | Thymeleaf |
| DB | H2（ファイル保存。インストール不要）／ テーブルは Flyway で作成 |
| ビルド | Maven（`mvnw` 付き。Maven のインストールは不要） |

---

## 1. はじめにやること（全員・1回だけ）

### 1-1. リポジトリを取ってくる
```
git clone <GitHubのURL>
```

### 1-2. Eclipse（Pleiades 2023-12）で開く
1. **ファイル > インポート > Maven > 既存 Maven プロジェクト** → `BaitoLog` フォルダを選ぶ
   - **「Dynamic Web Project」や「Tomcat への追加」はしない**（Spring Boot は Tomcat を内蔵している）
2. **プロジェクトを右クリック > プロパティ > Java のビルド・パス > ライブラリー** で JRE が **Java 21** になっているか確認
   - 違うとき：**ウィンドウ > 設定 > Java > インストール済みの JRE** で `D:\java\21`（Pleiades 付属）を追加して選ぶ
3. **ウィンドウ > 設定 > Java > コード・スタイル > フォーマッター > インポート** で、リポジトリ直下の `eclipse-formatter.xml` を読み込み、「BaitoLog」を選ぶ
   - **これをしないとインデントがタブになり、保存するたびにファイル全体が差分になる**
4. **ウィンドウ > 設定 > 一般 > ワークスペース** の「テキスト・ファイルのエンコード」が **UTF-8** か確認

### 1-3. IntelliJ IDEA Community で開く
1. **File > Open** → `BaitoLog/pom.xml` を選び「Open as Project」
2. **File > Project Structure > Project** で SDK を **21** にする
3. **Settings > Editor > Code Style > Java > ⚙ > Import Scheme > Eclipse XML Profile** で `eclipse-formatter.xml` を読み込む
4. 保存で自動再起動させたいとき（DevTools）
   - Settings > Build, Execution, Deployment > Compiler >「Build project automatically」をオン
   - Settings > Advanced Settings >「Allow auto-make to start even if developed application is currently running」をオン

---

## 2. 起動のしかた

| | 手順 |
|---|---|
| Eclipse | `src/main/java/com/baitolog/BaitoLogApplication.java` を右クリック > **実行 > Java アプリケーション**（または Spring Boot アプリケーション） |
| IntelliJ | `BaitoLogApplication` の横の ▶ を押す |
| コマンド | `mvnw.cmd spring-boot:run`（Windows）／ `./mvnw spring-boot:run` |

ブラウザで http://localhost:8080/ を開き、「起動できました」と出れば準備完了。

止めるときは Eclipse のコンソールの赤い■、IntelliJ の■。**止めずにもう一度起動すると「8080 番ポートが使用中」で失敗する**。

---

## 3. チームのルール

1. **Lombok は使わない**（PCごとの設定が必要で、入っていない人だけエラーになる）
2. **ファイル全体の一括整形（Eclipse: Ctrl+Shift+F／IntelliJ: Ctrl+Alt+L）は、自分が編集したファイルだけ**
3. **push する前に必ずビルドとテストを通す**（Eclipse と IntelliJ はコンパイラが違うため、最後は Maven で確認する）
   ```
   mvnw.cmd verify
   ```
4. **IDE の設定ファイル（.project / .classpath / .settings / .idea）はコミットしない**（.gitignore 済み）
5. **DB のテーブルを変えるときは `src/main/resources/db/migration` に新しい番号の SQL を足す**（コミット済みの SQL は書き換えない）

## 4. Git の進め方

- `main` には直接 push しない。作業ごとにブランチを作る
  ```
  git switch main
  git pull
  git switch -c feature/shift-edit
  ```
- 作業が終わったら push して GitHub で **プルリクエスト** を作り、**ほかのメンバー1人が確認してからマージ**する
- ブランチ名の例：`feature/login`、`feature/shift-edit`、`fix/seven-day-judge`
- コミットメッセージは日本語でよい。何をしたか1行で（例：「シフト登録画面の入力チェックを追加」）

## 5. フォルダ構成（予定）

```
src/main/java/com/baitolog/
  BaitoLogApplication.java  … 起動クラス
  config/                   … 設定（SecurityConfig など）
  controller/               … 画面のURLを受け付けるクラス（シーケンス図のビュー）
  service/                  … 判定・計算（SevenDayJudge、IncomeCalculator など）
  entity/                   … テーブルに対応するクラス
  repository/               … DB検索
  form/                     … 画面の入力と入力チェック
src/main/resources/
  templates/                … 画面（Thymeleaf の HTML）
  static/css/               … CSS
  db/migration/             … テーブルを作る SQL（Flyway）
```

設計書（シーケンス図・クラス図・テーブル定義書）はチームの共有フォルダにある。設計書を変えたら、コードとどちらも直すこと。
