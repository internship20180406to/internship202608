-- investmentTrust 機能拡張用スキーマ(銘柄価格の自動更新シミュレーション フェーズ3)
-- MySQLのinternshipデータベースに対して手動で実行すること(schema_v6.sql適用済みが前提、Spring Bootからの自動実行は行わない)

-- 銘柄価格のシミュレーション履歴(1時間ごとにバッチ処理で1件ずつ追加される。追記のみで上書きしないため、そのまま更新履歴になる)
-- 【注意】これは実際の投資信託の値動きを再現するものではなく、デモ表示用に前回価格から±2%程度をランダムに変動させる簡易シミュレーションである。
-- 概算口数・確定口数の計算にはこのテーブルは使用せず、引き続き fund_nav(行員が登録する約定日時点の正式な基準価額)を使用する。
CREATE TABLE fund_price_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    fund_code VARCHAR(20) NOT NULL,
    price INT NOT NULL,
    recorded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (fund_code) REFERENCES fund_master (fund_code),
    KEY idx_fund_recorded (fund_code, recorded_at)
);

-- 初期価格(全銘柄に1件ずつ投入。以降は1時間ごとのバッチ処理でここから変動させていく)
INSERT INTO fund_price_history (fund_code, price)
SELECT fund_code, 10000 FROM fund_master;
