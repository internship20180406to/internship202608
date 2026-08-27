-- ============================================================================
-- 既存の投資信託申込テーブルに、金融機関コード・支店コードを追加する
--
-- 実行方法:
--   mysql -uroot -p --default-character-set=utf8mb4 internship < investmentTrust/src/main/resources/db/04_alter.sql
--
-- ★このファイルは一度だけ実行すること。
--   MySQLの ALTER TABLE には ADD COLUMN IF NOT EXISTS が無い（MariaDBにはある）ため、
--   他のファイルのように「何度流しても大丈夫」な書き方ができない。
--   2回目を実行すると「Duplicate column name 'bankCode'」で止まる。
--   ※適用済みかどうかを自動で管理してくれる道具がFlywayなどのマイグレーションツール。
--
-- ★実行順序が重要。02_master_data.sql を先に流しておくこと。
--   既存データの名称（山陰共同銀行・和白支店）からコードを逆引きするので、
--   マスタにその名称が入っていないと埋められない。
-- ============================================================================
SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- 1) まずNULL許容で列を追加する
--
-- いきなり NOT NULL を付けると、既存行に入れる値が無いので失敗する。
-- 「NULL許容で追加 → 値を埋める → NOT NULLに変更」が定石。
--
-- 明細テーブルにはマスタへの外部キーをあえて張らない。
-- コードと名称の両方を保存する「申込時点のスナップショット」にしておくと、
-- 将来マスタから消えた金融機関の申込データもそのまま残せるため。
-- ----------------------------------------------------------------------------
ALTER TABLE investmenttrust_table
    ADD COLUMN bankCode   CHAR(4) NULL COMMENT '金融機関コード4桁' AFTER id,
    ADD COLUMN branchCode CHAR(3) NULL COMMENT '支店コード3桁'     AFTER bankCode;

-- ----------------------------------------------------------------------------
-- 2) 既存行のコードを、名称から逆引きして埋める
-- ----------------------------------------------------------------------------
UPDATE investmenttrust_table t
       JOIN bank_master b ON b.bankName = t.bankName
   SET t.bankCode = b.bankCode;

-- 支店は bankCode と branchName の両方で絞り込む。
-- 支店名は銀行をまたいで重複しうるので、branchName だけで探してはいけない。
UPDATE investmenttrust_table t
       JOIN branch_master br ON br.bankCode   = t.bankCode
                            AND br.branchName = t.branchName
   SET t.branchCode = br.branchCode;

-- ----------------------------------------------------------------------------
-- 3) 埋め残しの確認
--
-- ここで行が表示されたら、その行の名称がマスタに存在しない、ということ。
-- マスタに追加してから 2) のUPDATEをもう一度実行する。
-- （埋め残したまま 05_not_null.sql に進むと、そちらのNOT NULL化がエラーで止まる。
--   それが正しい挙動）
-- ----------------------------------------------------------------------------
SELECT id, bankCode, bankName, branchCode, branchName
  FROM investmenttrust_table
 WHERE bankCode IS NULL OR branchCode IS NULL;

-- ----------------------------------------------------------------------------
-- 4) NOT NULL化は、このファイルでは行わない
--
-- ここで NOT NULL にすると、その瞬間から今のアプリで申込ができなくなる。
-- InvestmentTrustRepository のINSERT文はまだ bankCode / branchCode を指定していないため、
-- 「Field 'bankCode' doesn't have a default value」で失敗するため。
--
-- NULL許容のままにしておけば、アプリを改修するまでの間も申込は通る
-- （新しい行はコード列がNULLで入る）。
-- Repositoryの改修が終わってから 05_not_null.sql を実行すること。
-- ----------------------------------------------------------------------------

-- 確認用
SELECT id, bankCode, bankName, branchCode, branchName, bankAccountType, bankAccountNum
  FROM investmenttrust_table;
