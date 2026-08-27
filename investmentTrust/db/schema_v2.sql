-- investmentTrust 機能拡張用スキーマ
-- MySQLのinternshipデータベースに対して手動で実行すること(Spring Bootからの自動実行は行わない)

-- 既存の investmentTrust_table を作り直す(カラム名とEntityフィールド名の不一致を解消するため)
DROP TABLE IF EXISTS investmentTrust_table;
DROP TABLE IF EXISTS branch_master;
DROP TABLE IF EXISTS institution_master;
DROP TABLE IF EXISTS fund_master;

-- 金融機関マスタ
CREATE TABLE institution_master (
    institution_code VARCHAR(10) NOT NULL,
    institution_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (institution_code)
);

-- 支店マスタ(金融機関に従属)
CREATE TABLE branch_master (
    institution_code VARCHAR(10) NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    branch_name VARCHAR(100) NOT NULL,
    PRIMARY KEY (institution_code, branch_code),
    FOREIGN KEY (institution_code) REFERENCES institution_master (institution_code)
);

-- 銘柄マスタ(購入手数料率を保持)
CREATE TABLE fund_master (
    fund_code VARCHAR(20) NOT NULL,
    fund_name VARCHAR(200) NOT NULL,
    purchase_fee_rate DECIMAL(5,3) NOT NULL,
    PRIMARY KEY (fund_code)
);

-- 投資信託注文テーブル(コードで保存し、名称はマスタとJOINして解決する)
CREATE TABLE investmentTrust_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    institution_code VARCHAR(10) NOT NULL,
    branch_code VARCHAR(10) NOT NULL,
    bank_account_num VARCHAR(7) NOT NULL,
    bank_subject VARCHAR(20) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    address VARCHAR(200) NOT NULL DEFAULT '',
    contact VARCHAR(50) NOT NULL DEFAULT '',
    fund_code VARCHAR(20) NOT NULL,
    purchase_amount INT NOT NULL,
    purchase_fee INT NOT NULL,
    order_datetime DATETIME NOT NULL,
    trade_date DATE NOT NULL,
    FOREIGN KEY (institution_code, branch_code) REFERENCES branch_master (institution_code, branch_code),
    FOREIGN KEY (fund_code) REFERENCES fund_master (fund_code)
);

-- 金融機関マスタ データ
INSERT INTO institution_master (institution_code, institution_name) VALUES
    ('0001', '海銀行'),
    ('0002', '山陰共同銀行'),
    ('0003', '流れ星銀行'),
    ('0004', 'ハレルヤ銀行');

-- 支店マスタ データ(各金融機関に同じ4支店を用意)
INSERT INTO branch_master (institution_code, branch_code, branch_name)
SELECT institution_code, branch_code, branch_name
FROM institution_master
CROSS JOIN (
    SELECT '001' AS branch_code, '小倉店' AS branch_name
    UNION ALL SELECT '002', '福岡店'
    UNION ALL SELECT '003', '久留米店'
    UNION ALL SELECT '004', '飯塚店'
) branches;

-- 銘柄マスタ データ(購入手数料率は銘柄ごとに変える)
INSERT INTO fund_master (fund_code, fund_name, purchase_fee_rate) VALUES
    ('F001', 'モビリティ・イノベーション・ファンド', 0.033),
    ('F002', 'HSBCインドオープン', 0.030),
    ('F003', '新興国連続増配成長株オープン', 0.028),
    ('F004', '日本高配当リバランスオープン', 0.022),
    ('F005', '損保ジャパン・グリーン・オープン 愛称：ぶなの森', 0.025),
    ('F006', 'みずほ好配当日本株オープン', 0.022);
