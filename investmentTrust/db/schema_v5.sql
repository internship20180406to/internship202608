-- investmentTrust 機能拡張用スキーマ(顧客管理・申し込み履歴の基盤 フェーズ1)
-- MySQLのinternshipデータベースに対して手動で実行すること(schema_v4.sql適用済みが前提、Spring Bootからの自動実行は行わない)

-- 顧客マスタ(ログイン機構がないため、姓名+連絡先の組み合わせが一致する申し込みを同一顧客とみなす簡易的な名寄せ)
CREATE TABLE customer_master (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    address VARCHAR(200) NOT NULL DEFAULT '',
    contact VARCHAR(50) NOT NULL DEFAULT '',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_customer_identity (last_name, first_name, contact)
);

-- 既存の注文データを顧客マスタに取り込む(姓名+連絡先が同じ行は1顧客にまとめる)
INSERT INTO customer_master (last_name, first_name, address, contact)
SELECT last_name, first_name, MIN(address), contact
FROM investmentTrust_table
GROUP BY last_name, first_name, contact;

-- 注文テーブルに顧客IDを追加し、既存データを顧客マスタへ紐付ける
ALTER TABLE investmentTrust_table
    ADD COLUMN customer_id BIGINT NULL AFTER id;

UPDATE investmentTrust_table t
JOIN customer_master c
    ON c.last_name = t.last_name AND c.first_name = t.first_name AND c.contact = t.contact
SET t.customer_id = c.customer_id;

ALTER TABLE investmentTrust_table
    MODIFY COLUMN customer_id BIGINT NOT NULL,
    ADD FOREIGN KEY (customer_id) REFERENCES customer_master (customer_id);
