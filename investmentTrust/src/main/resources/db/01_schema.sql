-- ============================================================================
-- 金融機関マスタ・支店マスタ・残高テーブルの定義
--
-- 実行方法（--default-character-set の指定は必須。付け忘れると日本語が壊れて入る）:
--   mysql -uroot -p --default-character-set=utf8mb4 internship < investmentTrust/src/main/resources/db/01_schema.sql
--
-- CREATE TABLE IF NOT EXISTS で書いているので、何度実行しても壊れない。
-- ※ ALTER TABLE（04_alter.sql）はMySQLでは IF NOT EXISTS が使えないため冪等にできない。
--    あちらは一度だけ実行すること。
-- ============================================================================
SET NAMES utf8mb4;

-- ----------------------------------------------------------------------------
-- 金融機関マスタ
--
-- bankCode … 金融機関コード。実在の体系に合わせて4桁固定。
--   ・CHAR(4) にしているのは「必ず4桁」だから。VARCHARだと '1' と '0001' の
--     両方が入ってしまい、検索のたびに桁を揃える処理が必要になる。
--   ・数値型ではなく文字列で持つのは、先頭の0が意味を持つため。
--     口座番号を int から char(7) に直したのと同じ理由（0031111 が 31111 になってしまう）。
-- bankKana … カナ検索用。全銀システムに合わせて半角カナで持つ。
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS bank_master (
    bankCode CHAR(4)     NOT NULL COMMENT '金融機関コード4桁',
    bankName VARCHAR(20) NOT NULL COMMENT '金融機関名',
    bankKana VARCHAR(40) NOT NULL COMMENT '金融機関名カナ（半角）',
    PRIMARY KEY (bankCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='金融機関マスタ';

-- ----------------------------------------------------------------------------
-- 支店マスタ
--
-- ★重要:主キーは (bankCode, branchCode) の複合主キーにする。
--   支店コードは「その銀行の中での通し番号」なので、別の銀行に同じ 001 が存在する。
--   branchCode 単独を主キーにすると、2行目の銀行の 001 を登録した時点で
--   重複エラーになり、作り直しになる。
--
-- 外部キーで bank_master と繋いでおくと、存在しない銀行の支店を
-- 登録しようとした時点でDBが弾いてくれる。
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS branch_master (
    bankCode   CHAR(4)     NOT NULL COMMENT '金融機関コード4桁',
    branchCode CHAR(3)     NOT NULL COMMENT '支店コード3桁',
    branchName VARCHAR(20) NOT NULL COMMENT '支店名',
    branchKana VARCHAR(40) NOT NULL COMMENT '支店名カナ（半角）',
    PRIMARY KEY (bankCode, branchCode),
    CONSTRAINT fk_branch_bank FOREIGN KEY (bankCode) REFERENCES bank_master (bankCode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='支店マスタ';

-- ----------------------------------------------------------------------------
-- 口座残高
--
-- 口座は「金融機関コード＋支店コード＋科目＋口座番号」の4点セットで識別する。
-- 口座番号だけでは一意にならない（別の銀行・別の支店に同じ番号が存在する）ので、
-- 4つすべてを主キーにする。
--
-- balance … 円は整数なのでBIGINT。int だと約21億で溢れる（残高は金額より大きくなりうる）。
--           CHECK制約で、バグで残高がマイナスになる更新をDB側でも止めている。
-- updatedAt … ON UPDATE CURRENT_TIMESTAMP を付けているので、
--             UPDATE文で明示的に指定しなくても自動で現在時刻に更新される。
--
-- accountNum は investmenttrust_table.bankAccountNum と同じ CHAR(7) にして
-- 突き合わせできるようにしている。
-- ----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS account_balance (
    bankCode    CHAR(4)     NOT NULL COMMENT '金融機関コード4桁',
    branchCode  CHAR(3)     NOT NULL COMMENT '支店コード3桁',
    accountType VARCHAR(5)  NOT NULL COMMENT '科目（普通/当座/貯蓄/その他）',
    accountNum  CHAR(7)     NOT NULL COMMENT '口座番号7桁',
    accountName VARCHAR(20) NOT NULL COMMENT '口座名義（半角カナ）',
    balance     BIGINT      NOT NULL DEFAULT 0 COMMENT '残高（円）',
    updatedAt   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (bankCode, branchCode, accountType, accountNum),
    CONSTRAINT fk_balance_branch FOREIGN KEY (bankCode, branchCode)
        REFERENCES branch_master (bankCode, branchCode),
    CONSTRAINT chk_balance_not_negative CHECK (balance >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
  COMMENT='口座残高';
