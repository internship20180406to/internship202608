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
| `01_schema.sql` | 金融機関マスタ・支店マスタ・口座残高テーブルの作成 | 何度でも可 |
| `02_master_data.sql` | 金融機関・支店の初期データ | 何度でも可 |
| `03_balance_data.sql` | 動作確認用の口座と残高（流し直すと残高が初期値に戻る） | 何度でも可 |
| `04_alter.sql` | 申込テーブルへのコード列追加と、既存データの移行 | **一度だけ** |
| `05_not_null.sql` | コード列を NOT NULL にする | **一度だけ・Repository改修後** |

`04_alter.sql` は2回目を実行すると `Duplicate column name 'bankCode'` で止まります。
MySQLの `ALTER TABLE` には `ADD COLUMN IF NOT EXISTS` が無いため、他のファイルのように
何度流しても大丈夫な書き方ができないためです。

`05_not_null.sql` は他の4本と違い、**初回セットアップでは実行しません**。
先に実行すると、`InvestmentTrustRepository` のINSERT文がまだコード列を指定していないため、
申込時に `Field 'bankCode' doesn't have a default value` で失敗するようになります。
Repositoryの改修が終わってから実行してください。

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

