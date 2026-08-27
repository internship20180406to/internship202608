-- ============================================================================
-- 金融機関コード・支店コードを NOT NULL にする
--
-- 実行方法:
--   mysql -uroot -p --default-character-set=utf8mb4 internship < investmentTrust/src/main/resources/db/05_not_null.sql
--
-- ★実行するタイミング:InvestmentTrustRepository のINSERT文に
--   bankCode / branchCode を追加し終わってから。
--
--   先に実行してしまうと、改修前のアプリからの申込が
--   「Field 'bankCode' doesn't have a default value」で失敗するようになる。
--   （列にデフォルト値が無く、かつMySQLがstrictモードのため）
--
-- ★このファイルも一度だけ実行すること。
--   2回目は「NOT NULLをNOT NULLにする」だけなのでエラーにはならないが、
--   テーブル全体が作り直されるので無駄が大きい。
-- ============================================================================
SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- 1) 埋め残しの確認
--
-- ここで行が表示されたら、その行はコードがNULLのまま。
-- NULLの行が1件でもあると、次のNOT NULL化が
-- 「Invalid use of NULL value」で失敗する（それが正しい挙動）。
--
-- 改修前のアプリで登録された申込が残っている場合は、
-- 04_alter.sql の 2) と同じUPDATEをもう一度流して埋めてから進むこと。
-- ----------------------------------------------------------------------------
SELECT id, bankCode, bankName, branchCode, branchName
  FROM investmenttrust_table
 WHERE bankCode IS NULL OR branchCode IS NULL;

-- ----------------------------------------------------------------------------
-- 2) NOT NULL にする
--    以降、コードの無い申込はDBが受け付けなくなる
-- ----------------------------------------------------------------------------
ALTER TABLE investmenttrust_table
    MODIFY COLUMN bankCode   CHAR(4) NOT NULL COMMENT '金融機関コード4桁',
    MODIFY COLUMN branchCode CHAR(3) NOT NULL COMMENT '支店コード3桁';

-- ----------------------------------------------------------------------------
-- 3) 金融機関名の桁数をマスタに合わせる
--
-- 申込テーブルの bankName は varchar(7) で作られていた。
-- 一方 bank_master.bankName は VARCHAR(20) なので、8文字以上の金融機関名を
-- マスタに追加した瞬間、申込のINSERTが桁あふれで失敗するようになる。
-- 今のマスタは最長6文字（山陰共同銀行）なので表面化していないが、先に揃えておく。
--
-- ※01_schema.sql は最初から VARCHAR(20) で作るので、
--   新規環境と既存環境がこの時点で同じ形になる。
-- ----------------------------------------------------------------------------
ALTER TABLE investmenttrust_table
    MODIFY COLUMN bankName VARCHAR(20) NULL COMMENT '申込時点の金融機関名';

-- 確認用
SHOW COLUMNS FROM investmenttrust_table;
