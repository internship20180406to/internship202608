-- ============================================================
--  bankTransfer のスキーマ
--  アプリからは自動実行しない。MySQL へ手動で流す想定。
--    mysql -h127.0.0.1 -uroot -p internship < db/schema.sql
-- ============================================================

-- ------------------------------------------------------------
-- 金融機関マスタ
--   bankName の長さは bankTransfer_table.bankName(varchar(7)) に合わせている。
--   ここを広げるなら、あちらも同時に広げないと登録時にSQLエラーになる。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bank_master (
    bankCode CHAR(4)    NOT NULL,
    bankName VARCHAR(7) NOT NULL,
    -- 画面の一覧に出すかどうか。出さないものは検索でのみ到達できる
    isMajor  TINYINT(1) NOT NULL DEFAULT 0,
    PRIMARY KEY (bankCode),
    UNIQUE KEY uk_bank_master_name (bankName)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ------------------------------------------------------------
-- 支店マスタ
--   支店コードは銀行の中でだけ一意。銀行をまたぐと重複するので複合主キーにする。
--   branchName の長さは bankTransfer_table.branchName(varchar(20)) に合わせている。
-- ------------------------------------------------------------
CREATE TABLE IF NOT EXISTS branch_master (
    bankCode   CHAR(4)     NOT NULL,
    branchCode CHAR(3)     NOT NULL,
    branchName VARCHAR(20) NOT NULL,
    PRIMARY KEY (bankCode, branchCode),
    CONSTRAINT fk_branch_master_bank
        FOREIGN KEY (bankCode) REFERENCES bank_master (bankCode)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- ------------------------------------------------------------
-- 申し込み記録にコードを追加
--   既存の行にはコードが無いので NULL 可にしている。
-- ------------------------------------------------------------
ALTER TABLE bankTransfer_table
    ADD COLUMN bankCode   CHAR(4) NULL AFTER id,
    ADD COLUMN branchCode CHAR(3) NULL AFTER bankName;

-- ------------------------------------------------------------
-- 初期データ：銀行6行
-- ------------------------------------------------------------
INSERT INTO bank_master (bankCode, bankName, isMajor) VALUES
    ('0001', 'AAA銀行', 1),
    ('0002', 'BBB銀行', 1),
    ('0003', 'CCC銀行', 1),
    ('0004', 'DDD銀行', 0),
    ('0005', 'EEE銀行', 0),
    ('0006', 'FFF銀行', 0);

-- ------------------------------------------------------------
-- 初期データ：支店 各銀行9店（計54店）
--   支店名は銀行の頭文字＋連番。AAA銀行なら A1支店〜A9支店。
-- ------------------------------------------------------------
INSERT INTO branch_master (bankCode, branchCode, branchName)
SELECT b.bankCode,
       LPAD(n.num, 3, '0'),
       CONCAT(LEFT(b.bankName, 1), n.num, '支店')
FROM bank_master b
CROSS JOIN (
    SELECT 1 AS num UNION ALL SELECT 2 UNION ALL SELECT 3
    UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6
    UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9
) n;

-- ------------------------------------------------------------
-- 申し込み記録に利用者を追加
--   履歴と登録先は「その人のもの」しか見せてはいけないので、
--   誰の記録かをここで持つ。ログインが無い間は 'demo' が入る。
-- ------------------------------------------------------------
ALTER TABLE bankTransfer_table
    ADD COLUMN userId VARCHAR(32) NULL AFTER id;

UPDATE bankTransfer_table SET userId = 'demo' WHERE userId IS NULL;

ALTER TABLE bankTransfer_table
    MODIFY COLUMN userId VARCHAR(32) NOT NULL;

CREATE INDEX idx_bankTransfer_user ON bankTransfer_table (userId, id DESC);
