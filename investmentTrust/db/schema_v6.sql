-- investmentTrust 機能拡張用スキーマ(基準価額マスタ・口数計算・ステータス変更履歴 フェーズ2)
-- MySQLのinternshipデータベースに対して手動で実行すること(schema_v5.sql適用済みが前提、Spring Bootからの自動実行は行わない)

-- 基準価額マスタ(行員モードから手動登録。1万口あたりの基準価額)
-- 概算口数(申込時点で直近日以前の最新値を使用)・確定口数(約定日と完全一致する値を使用)の両方に使う
CREATE TABLE fund_nav (
    fund_code VARCHAR(20) NOT NULL,
    nav_date DATE NOT NULL,
    nav_value INT NOT NULL,
    PRIMARY KEY (fund_code, nav_date),
    FOREIGN KEY (fund_code) REFERENCES fund_master (fund_code)
);

-- 初期データ: 全銘柄に基準日時点の基準価額を1件ずつ投入(未登録だと概算口数が計算できないため)
INSERT INTO fund_nav (fund_code, nav_date, nav_value)
SELECT fund_code, '2026-08-01', 12000 FROM fund_master;

-- 注文ステータスの変更履歴(いつ・誰が処理したか)
CREATE TABLE order_status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    changed_by VARCHAR(50) NOT NULL,
    changed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (order_id) REFERENCES investmentTrust_table (id)
);

-- 注文テーブルに概算/確定口数を追加(1万口あたりの基準価額を基準とした口数)
ALTER TABLE investmentTrust_table
    ADD COLUMN estimated_units BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN confirmed_units BIGINT NULL;

-- 既存の注文データにも「申込受付」の履歴を1件ずつ補完しておく(履歴機構導入前のデータを整合させるため)
INSERT INTO order_status_history (order_id, status, changed_by, changed_at)
SELECT id, '申込受付', 'SYSTEM', order_datetime FROM investmentTrust_table;
