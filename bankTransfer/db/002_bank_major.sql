-- ============================================================
--  金融機関マスタに「主な金融機関かどうか」を追加する
--  既に db/schema.sql を流したDBに対して当てる差分。
--  新規に作る場合は schema.sql 側に取り込み済みなので不要。
--    mysql -h127.0.0.1 -uroot -p --default-character-set=utf8mb4 internship < db/002_bank_major.sql
-- ============================================================

-- 画面の一覧に出すかどうか。出さないものは検索でのみ到達できる
ALTER TABLE bank_master
    ADD COLUMN isMajor TINYINT(1) NOT NULL DEFAULT 0 AFTER bankName;

UPDATE bank_master SET isMajor = 1 WHERE bankCode IN ('0001', '0002', '0003');
