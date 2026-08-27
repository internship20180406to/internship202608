-- investmentTrust 機能拡張用スキーマ(約定タイミング管理)
-- MySQLのinternshipデータベースに対して手動で実行すること(schema_v2.sql適用済みが前提、Spring Bootからの自動実行は行わない)

-- 受付締切時間の設定(常に1行のみ保持し、行員モードの設定画面から更新する)
CREATE TABLE trade_cutoff_setting (
    id INT NOT NULL,
    cutoff_time TIME NOT NULL,
    PRIMARY KEY (id)
);

INSERT INTO trade_cutoff_setting (id, cutoff_time) VALUES (1, '15:00:00');

-- 非営業日カレンダー(土日祝・証券会社休業日・国内外市場休場日・ファンド休日をまとめて1本で管理)
-- reasonは表示・管理用の分類であり、約定日判定ではnon_business_dateの有無のみを見る(全種別を同列に扱う)
CREATE TABLE non_business_day (
    non_business_date DATE NOT NULL,
    reason VARCHAR(100) NOT NULL,
    PRIMARY KEY (non_business_date)
);

-- 2026年 国民の祝日(土日は別途曜日判定で除外するためここには含めない)
INSERT INTO non_business_day (non_business_date, reason) VALUES
    ('2026-01-01', '元日'),
    ('2026-01-12', '成人の日'),
    ('2026-02-11', '建国記念の日'),
    ('2026-02-23', '天皇誕生日'),
    ('2026-03-20', '春分の日'),
    ('2026-04-29', '昭和の日'),
    ('2026-05-04', 'みどりの日'),
    ('2026-05-05', 'こどもの日'),
    ('2026-05-06', '振替休日'),
    ('2026-07-20', '海の日'),
    ('2026-08-11', '山の日'),
    ('2026-09-21', '敬老の日'),
    ('2026-09-22', '秋分の日'),
    ('2026-10-12', 'スポーツの日'),
    ('2026-11-03', '文化の日'),
    ('2026-11-23', '勤労感謝の日');

-- 証券会社休業日(年末年始の取引所休業。1/1は元日として既に登録済み)
INSERT INTO non_business_day (non_business_date, reason) VALUES
    ('2026-01-02', '証券会社休業日(年始)'),
    ('2026-01-03', '証券会社休業日(年始)'),
    ('2026-12-31', '証券会社休業日(年末)');

-- 海外市場休場日(例: NYSEのクリスマス休場)
INSERT INTO non_business_day (non_business_date, reason) VALUES
    ('2026-12-25', '海外市場休場日(クリスマス)');
