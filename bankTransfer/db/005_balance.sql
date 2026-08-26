-- ============================================================
--  口座残高
--  既に db/schema.sql を流したDBに対して当てる差分。
--  新規に作る場合は schema.sql 側に取り込み済みなので不要。
--    mysql -uroot -p --default-character-set=utf8mb4 internship < db/005_balance.sql
--
--  amount を UNSIGNED にしているのは保険。引きすぎたときに
--  マイナスの残高が静かに残らず、SQLエラーとして表に出る。
-- ============================================================

CREATE TABLE IF NOT EXISTS balance (
    userId VARCHAR(32)  NOT NULL,
    amount INT UNSIGNED NOT NULL,
    PRIMARY KEY (userId)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

-- 既定の利用者に初期残高を入れる。既にあれば触らない
INSERT INTO balance (userId, amount) VALUES ('demo', 1000000)
    ON DUPLICATE KEY UPDATE amount = amount;
