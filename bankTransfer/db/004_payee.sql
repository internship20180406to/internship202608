-- ============================================================
--  振込先の登録
--  既に db/schema.sql を流したDBに対して当てる差分。
--  新規に作る場合は schema.sql 側に取り込み済みなので不要。
--    mysql -uroot -p --default-character-set=utf8mb4 internship < db/004_payee.sql
--
--  列の型は bankTransfer_table に合わせている。あちらを広げるなら
--  こちらも同時に広げないと、登録できたものが振り込めなくなる。
-- ============================================================

CREATE TABLE IF NOT EXISTS payee (
    id              INT UNSIGNED NOT NULL AUTO_INCREMENT,
    -- 誰の登録先か。一覧も削除も必ずこの列で絞る
    userId          VARCHAR(32)  NOT NULL,
    -- 画面で見分けるための呼び名。口座番号だけでは選びにくいため
    nickname        VARCHAR(20)  NOT NULL,
    bankCode        CHAR(4)      NOT NULL,
    bankName        VARCHAR(7)   NOT NULL,
    branchCode      CHAR(3)      NOT NULL,
    branchName      VARCHAR(20)  NOT NULL,
    bankAccountType VARCHAR(5)   NOT NULL,
    bankAccountNum  CHAR(7)      NOT NULL,
    name            VARCHAR(20)  NOT NULL,
    PRIMARY KEY (id),
    -- 同じ相手を二重に登録させない。振込先を決めるのはこの4つ。
    -- 利用者をまたいだ重複は正常なので、userId も鍵に含める
    UNIQUE KEY uk_payee (userId, bankCode, branchCode, bankAccountType, bankAccountNum),
    -- 一覧は「その利用者の分を新しい順に」引く
    KEY idx_payee_user (userId, id DESC)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
