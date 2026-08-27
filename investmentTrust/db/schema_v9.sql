-- investmentTrust 機能拡張用スキーマ(基準価額(NAV)手動登録・銘柄価格シミュレーションの廃止 → 固定基準価格への簡素化)
-- MySQLのinternshipデータベースに対して手動で実行すること(schema_v8.sql適用済みが前提、Spring Bootからの自動実行は行わない)

-- 行員モードが煩雑になっていたため、基準価額(NAV)の手動登録・銘柄価格の自動更新シミュレーションを廃止する。
-- 口数計算(概算/確定)は、銘柄マスタに持たせる固定の基準価格を使う簡易な方式に置き換える。

-- 銘柄マスタに口数計算用の固定基準価格を追加する(1万口あたり・円。実際の基準価額のような日次更新は行わない)
ALTER TABLE fund_master
    ADD COLUMN reference_price INT NOT NULL DEFAULT 12000;

-- 廃止した機能のテーブルを削除する
DROP TABLE IF EXISTS fund_price_history;
DROP TABLE IF EXISTS fund_nav;
