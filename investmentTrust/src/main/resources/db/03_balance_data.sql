-- ============================================================================
-- 口座残高の初期データ（動作確認用）
--
-- 実行方法:
--   mysql -uroot -p --default-character-set=utf8mb4 internship < investmentTrust/src/main/resources/db/03_balance_data.sql
--
-- 支店マスタへの外部キーがあるので、02_master_data.sql を先に流しておくこと。
--
-- ON DUPLICATE KEY UPDATE で残高を初期値に戻すようにしている。
-- 引き落としの動作確認で残高を減らしたあと、このファイルをもう一度流せば
-- 元の残高に戻せる。
-- ============================================================================
SET NAMES utf8mb4;

INSERT INTO account_balance
    (bankCode, branchCode, accountType, accountNum, accountName, balance) VALUES
    -- 既に investmenttrust_table に入っている申込データの口座。
    -- これが無いと、その口座で申し込んでも完了画面に残高が出せない。
    ('0001', '002', '貯蓄', '0031111', 'ｵｶﾈ ﾅｲ',   10000000),
    -- 通常の動作確認用
    ('0001', '002', '普通', '1234567', 'ﾔﾏﾀﾞ ﾀﾛｳ',  5000000),
    ('0001', '001', '当座', '7654321', 'ｽｽﾞｷ ﾊﾅｺ',  3000000),
    ('0002', '005', '普通', '2222222', 'ｺﾌﾞﾀ ｲﾁﾛｳ', 1000000),
    -- 残高不足の動作確認用（最低購入額10,000円は満たせるが、すぐ足りなくなる）
    ('0002', '012', '普通', '9999999', 'ﾉｺﾘ ｽｸﾅｲ',    15000)
AS new
ON DUPLICATE KEY UPDATE
    accountName = new.accountName,
    balance     = new.balance;
