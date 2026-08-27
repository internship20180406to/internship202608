# インターンシップ課題
インターンの課題として、下記の簡素的なWebアプリを作成しています
- ローン申し込み
- 銀行振込
- 投信購入

## DBセットアップ
マスタ・残高テーブルの定義と初期データは `investmentTrust/src/main/resources/db/` 配下の
SQLファイルで管理しています。初回のみ、下記を**上から順に**実行してください。

```
cd investmentTrust/src/main/resources/db
mysql -uroot -p --default-character-set=utf8mb4 internship < 01_schema.sql
mysql -uroot -p --default-character-set=utf8mb4 internship < 02_master_data.sql
mysql -uroot -p --default-character-set=utf8mb4 internship < 03_balance_data.sql
mysql -uroot -p --default-character-set=utf8mb4 internship < 04_alter.sql
```

`--default-character-set=utf8mb4` は必須です。付け忘れるとWindowsの `mysql` は cp932 で接続するため、
日本語が壊れた状態で登録されます。MySQL Workbench で開いて実行する場合はこの指定は不要です（UTF-8で扱われます）。

| ファイル | 内容 | 再実行 |
|---|---|---|
| `01_schema.sql` | 金融機関マスタ・支店マスタ・口座残高・**投資信託の申込**テーブルの作成 | 何度でも可 |
| `02_master_data.sql` | 金融機関・支店の初期データ | 何度でも可 |
| `03_balance_data.sql` | 動作確認用の口座と残高（流し直すと残高が初期値に戻る） | 何度でも可 |
| `04_alter.sql` | 申込テーブルへのコード列追加と、既存データの移行 | **一度だけ・既存環境のみ** |
| `05_not_null.sql` | コード列を NOT NULL にし、桁数をマスタに合わせる | **一度だけ・Repository改修後** |

新しく作った環境では `04_alter.sql` は実行しません。`01_schema.sql` が最初からコード列を含む形で
申込テーブルを作るためです（流すと `Duplicate column name 'bankCode'` になるだけ）。
必要なのは「コード列が無い時代のテーブルが既にある環境」だけです。

テーブル定義は `01_schema.sql` に集約しています。**リポジトリ外の古い create table スクリプトは使わないでください。**
流すとコード列の無い構造に戻り、申込時に `Unknown column 'bankCode'` で失敗するようになります。

`04_alter.sql` は2回目を実行すると `Duplicate column name 'bankCode'` で止まります。
MySQLの `ALTER TABLE` には `ADD COLUMN IF NOT EXISTS` が無いため、他のファイルのように
何度流しても大丈夫な書き方ができないためです。

`05_not_null.sql` は他の4本と違い、**初回セットアップでは実行しません**。
先に実行すると、`InvestmentTrustRepository` のINSERT文がまだコード列を指定していないため、
申込時に `Field 'bankCode' doesn't have a default value` で失敗するようになります。
Repositoryの改修が終わってから実行してください。

## テスト
テストは開発用の `internship` ではなく、**テスト専用DB `internship_test`** に接続します
（接続先は `investmentTrust/src/test/resources/application.yml` で切り替えています）。
そのため、テストを何度実行しても画面で確認しているデータが増えたり残高が減ったりしません。

### テスト用DBの作成（初回のみ）

```
mysql -uroot -p -e "CREATE DATABASE internship_test DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci"
cd investmentTrust/src/main/resources/db
mysql -uroot -p --default-character-set=utf8mb4 internship_test < 01_schema.sql
mysql -uroot -p --default-character-set=utf8mb4 internship_test < 02_master_data.sql
mysql -uroot -p --default-character-set=utf8mb4 internship_test < 03_balance_data.sql
mysql -uroot -p --default-character-set=utf8mb4 internship_test < 05_not_null.sql
```

`04_alter.sql` は実行しません。`01_schema.sql` が最初からコード列を含む形でテーブルを作るためです。

### 実行

```
cd investmentTrust
./mvnw test
```

| テストクラス | 内容 | DB |
|---|---|---|
| `InvestmentTrustFormValidationTest` | 入力チェック（アノテーション）。Springを起動しない | 不要 |
| `InvestmentTrustControllerTest` | 確認画面へ進むときのサーバ側チェック | 参照のみ |
| `InvestmentTrustCompletionTest` | 申込の確定・残高引き落とし・二重送信対策 | 更新あり |
| `AccountBalanceRepositoryTest` | 残高の引き落とし・複合キーの判定 | 更新あり |
| `OrderInvestmentTrustServiceTest` | 残高不足時にトランザクションごと成立しないこと | 更新あり |

更新を伴うテストには `@Transactional` が付いており、各テストの最後に自動でロールバックされます。
テスト用DBのデータが壊れた場合は、上のセットアップ手順を流し直せば元に戻ります。

### 開発用DBのデータを初期状態に戻す
画面から申込を試して残高が減った場合は、下記で初期値に戻せます。

```
cd investmentTrust/src/main/resources/db
mysql -uroot -p --default-character-set=utf8mb4 internship < 03_balance_data.sql
```

申込データそのものを消す場合は `DELETE FROM investmenttrust_table;` を実行してください。

## 動作方法
ローンの場合
1. `internship\bankLoan\src\main\java\com\example\internship\InternshipApplication.java`に行く
2. ▶ を押し、実行を押す（二回目からは右上の▶や`shift`+`F10`で最近起動したものを再起動できる）
3. 実行後、実行ログで `Started InternshipApplication`を確認する
4. `http://localhost:8081/bankLoan` にブラウザでアクセス

銀行振込の場合
1. `internship\bankTransfer\src\main\java\com\example\internship\InternshipApplication.java`に行く
2. ▶ を押し、実行を押す（二回目からは右上の▶や`shift`+`F10`で最近起動したものを再起動できる）
3. 実行後、実行ログで `Started InternshipApplication`を確認する
4. `http://localhost:8082/bankTransfer` にブラウザでアクセス

投資信託の場合
1. `internship\investmentTrust\src\main\java\com\example\internship\InternshipApplication.java`に行く
2. ▶ を押し、実行を押す（二回目からは右上の▶や`shift`+`F10`で最近起動したものを再起動できる）
3. 実行後、実行ログで `Started InternshipApplication`を確認する
4. `http://localhost:8083/investmentTrust` にブラウザでアクセス

commit test

