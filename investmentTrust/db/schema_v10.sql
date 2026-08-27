-- investmentTrust 機能拡張用スキーマ(お客様向け注文履歴確認を、姓名+連絡先の一致から「連絡先+パスワード」でのログイン形式に変更)
-- MySQLのinternshipデータベースに対して手動で実行すること(schema_v9.sql適用済みが前提、Spring Bootからの自動実行は行わない)

-- 顧客マスタにログイン用パスワード(アプリ側でBCryptハッシュ化してから保存する)を追加する。
-- 移行前に作成された既存顧客は空文字のままとし、次回申し込み時に入力されたパスワードで初めて設定される(アプリ側のCustomerRepositoryで対応)。
ALTER TABLE customer_master
    ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT '';
