-- ============================================================
--  発表用のモックデータ：振込の履歴12行（相手は10人）
--    mysql -h127.0.0.1 -uroot -p --default-character-set=utf8mb4 internship < db/008_demo_history.sql
--
--  相手は福岡ソフトバンクホークスの背番号12〜23（15番・21番は空き番号）。
--  007 の登録済み（背番号2〜11）とは別の人にしてあるので、
--  「履歴」タブと「登録済み」タブで違う顔ぶれが出る。
--
--  10人のうち2人（東浜・周東）は2回振り込んだことにしてある。
--  履歴は同じ相手を1件にまとめるので、一覧には10件出て、
--  金額と日付は新しいほうの回のものが出る。まとめの動きを見せるための行。
--
--  手数料は実装と同じ決まりで入れている。
--    自行（ふくよか銀行 0177）あては 0円 / 30,000円未満は 220円 / 以上は 330円
-- ============================================================

-- demo の履歴を入れ替える。他の利用者の分は触らない
DELETE FROM bankTransfer_table WHERE userId = 'demo';

-- 金融機関名と支店名はマスタから引く（支店が銀行に属さない組み合わせは行ごと落ちる）。
-- 一覧の並びは振込指定日の新しい順。日付を散らしてあるので、
-- 上から 周東・藤原・大津・東浜・徐・稲川・嶺井・張・ラトリッジ・村上 の順に出る
INSERT INTO bankTransfer_table (userId, bankCode, bankName, branchCode, branchName,
                                bankAccountType, bankAccountNum, name, money, fee, transferDateTime)
SELECT 'demo', v.bankCode, b.bankName, v.branchCode, br.branchName,
       v.bankAccountType, v.bankAccountNum, v.name, v.money, v.fee, v.transferDateTime
  FROM (
            SELECT '0009' AS bankCode, '003' AS branchCode, '普通' AS bankAccountType,
                   '2094518' AS bankAccountNum, 'ﾐﾈｲ ﾋﾛｷ' AS name,
                    45000 AS money, 330 AS fee, '2026-07-14 00:00:00' AS transferDateTime
  UNION ALL SELECT '0177', '004', '普通', '6733012', 'ｲﾅｶﾞﾜ ﾘｭｳﾀ',   12000,   0, '2026-08-03 00:00:00'
  UNION ALL SELECT '0008', '005', '普通', '8150467', 'ﾗﾄﾘｯｼﾞ',        8000, 220, '2026-06-28 00:00:00'
  UNION ALL SELECT '0010', '002', '普通', '3428790', 'ﾄｳﾊﾏ ｵ',       20000, 220, '2026-07-08 00:00:00'
  UNION ALL SELECT '0010', '002', '普通', '3428790', 'ﾄｳﾊﾏ ｵ',      150000, 330, '2026-08-19 00:00:00'
  UNION ALL SELECT '0009', '006', '当座', '5567123', 'ﾁｮｳ ｼｭﾝｳｪｲ',   25000, 220, '2026-07-02 00:00:00'
  UNION ALL SELECT '0177', '007', '普通', '9012345', 'ｼﾞｮ ｼﾞｬｸｷ',    30000,   0, '2026-08-11 00:00:00'
  UNION ALL SELECT '0008', '001', '普通', '1246803', 'ｵｵﾂ ﾘｮｳｽｹ',     5500, 220, '2026-08-24 00:00:00'
  UNION ALL SELECT '0010', '007', '貯蓄', '7789456', 'ﾑﾗｶﾐ ﾀｲﾄ',     60000, 330, '2026-06-15 00:00:00'
  UNION ALL SELECT '0009', '002', '普通', '4321098', 'ﾌｼﾞﾜﾗ ﾊﾙﾄ',     3000, 220, '2026-08-26 00:00:00'
  UNION ALL SELECT '0177', '003', '普通', '5678901', 'ｼｭｳﾄｳ ﾕｳｷｮｳ',  80000,   0, '2026-05-20 00:00:00'
  UNION ALL SELECT '0177', '003', '普通', '5678901', 'ｼｭｳﾄｳ ﾕｳｷｮｳ', 200000,   0, '2026-08-27 00:00:00'
       ) v
  JOIN bank_master   b  ON b.bankCode  = v.bankCode
  JOIN branch_master br ON br.bankCode = v.bankCode
                       AND br.branchCode = v.branchCode
 ORDER BY v.transferDateTime;

-- 確認：12行入り、一覧では10件にまとまること
SELECT COUNT(*) AS 行数 FROM bankTransfer_table WHERE userId = 'demo';
SELECT name AS 口座名義, bankName AS 金融機関, branchName AS 支店,
       MAX(transferDateTime) AS 最終振込日, COUNT(*) AS 回数
  FROM bankTransfer_table WHERE userId = 'demo'
 GROUP BY bankCode, branchCode, bankAccountType, bankAccountNum, name, bankName, branchName
 ORDER BY 最終振込日 DESC;
