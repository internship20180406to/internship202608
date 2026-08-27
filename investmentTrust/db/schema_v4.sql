-- investmentTrust 機能拡張用スキーマ(手数料表示拡張・注文ステータス管理)
-- MySQLのinternshipデータベースに対して手動で実行すること(schema_v3.sql適用済みが前提、Spring Bootからの自動実行は行わない)

-- 銘柄マスタに信託報酬率(年率)・信託財産留保額率を追加する
-- 購入時手数料と違い、この2つは購入代金から直接差し引かれるものではなく、確認画面での参考開示用
ALTER TABLE fund_master
    ADD COLUMN trust_fee_rate DECIMAL(5,3) NOT NULL DEFAULT 0.000 AFTER purchase_fee_rate,
    ADD COLUMN redemption_reserve_rate DECIMAL(5,3) NOT NULL DEFAULT 0.000 AFTER trust_fee_rate;

-- 銘柄ごとの信託報酬率・信託財産留保額率を設定(実際の投資信託の相場観を参考にした目安値)
UPDATE fund_master SET trust_fee_rate = 0.020, redemption_reserve_rate = 0.000 WHERE fund_code = 'F001';
UPDATE fund_master SET trust_fee_rate = 0.021, redemption_reserve_rate = 0.003 WHERE fund_code = 'F002';
UPDATE fund_master SET trust_fee_rate = 0.019, redemption_reserve_rate = 0.003 WHERE fund_code = 'F003';
UPDATE fund_master SET trust_fee_rate = 0.011, redemption_reserve_rate = 0.000 WHERE fund_code = 'F004';
UPDATE fund_master SET trust_fee_rate = 0.017, redemption_reserve_rate = 0.000 WHERE fund_code = 'F005';
UPDATE fund_master SET trust_fee_rate = 0.010, redemption_reserve_rate = 0.000 WHERE fund_code = 'F006';

-- 注文にステータスを持たせる(申込受付→発注済み。約定は「発注済み かつ 約定日到来」で画面側が自動判定するため列としては持たない)
ALTER TABLE investmentTrust_table
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT '申込受付';
