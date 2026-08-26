-- ============================================================
--  申し込み記録に「誰の振込か」を追加する
--  既に db/schema.sql を流したDBに対して当てる差分。
--  新規に作る場合は schema.sql 側に取り込み済みなので不要。
--    mysql -uroot -p --default-character-set=utf8mb4 internship < db/003_user_id.sql
--
--  履歴と登録先は「その人のもの」しか見せてはいけないので、
--  誰の記録かをここで持つ。ログインが無い間は 'demo' が入る。
--
--  NULL可のまま放置すると古い行だけ欠けた状態が残るので、
--  既存行を埋めてから NOT NULL にする（bankCode で同じ問題を起こした反省）。
-- ============================================================

ALTER TABLE bankTransfer_table
    ADD COLUMN userId VARCHAR(32) NULL AFTER id;

UPDATE bankTransfer_table SET userId = 'demo' WHERE userId IS NULL;

ALTER TABLE bankTransfer_table
    MODIFY COLUMN userId VARCHAR(32) NOT NULL;

-- 履歴は「その利用者の分を新しい順に」引くので、その並びで索引を作る
CREATE INDEX idx_bankTransfer_user ON bankTransfer_table (userId, id DESC);
