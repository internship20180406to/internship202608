-- ============================================================
--  発表用のモックデータ：登録済みの振込先10件
--    mysql -h127.0.0.1 -uroot -p --default-character-set=utf8mb4 internship < db/007_demo_payees.sql
--
--  呼び名は福岡ソフトバンクホークスの背番号2〜11（1番は空き番号）。
--  口座名義は画面の決まりに合わせて半角カタカナで入れている。
--
--  金融機関は 006 で入れたデモ用の4行から選び、うち3件は自行（ふくよか銀行）
--  にしてある。自行あては手数料が無料なので、手数料の有無を並べて見せられる。
-- ============================================================

-- demo の登録先を入れ替える。他の利用者の分は触らない
DELETE FROM payee WHERE userId = 'demo';

-- 金融機関名と支店名はマスタから引く。
-- ここで名前を直書きすると、マスタと食い違った登録先ができてしまい、
-- 画面の表示と絞り込みがずれる。支店コードが銀行に属していなければ
-- JOIN で行が落ちるので、下の確認で件数が合わなくなる。
--
-- 並び順（seq）は「先に入れたものほど古い」。一覧は id の降順に出すので、
-- 背番号の小さい選手を上に見せるには、大きい選手から先に入れる
INSERT INTO payee (userId, nickname, bankCode, bankName, branchCode, branchName,
                   bankAccountType, bankAccountNum, name)
SELECT 'demo', v.nickname, v.bankCode, b.bankName, v.branchCode, br.branchName,
       v.bankAccountType, v.bankAccountNum, v.name
  FROM (
            SELECT  1 AS seq, '津森宥紀' AS nickname, '0008' AS bankCode, '002' AS branchCode,
                    '普通' AS bankAccountType, '1795082' AS bankAccountNum, 'ﾂﾓﾘ ﾕｳｷ' AS name
  UNION ALL SELECT  2, '上沢直之',             '0177', '001', '普通', '8236401', 'ｳｴｻﾜ ﾅｵﾕｷ'
  UNION ALL SELECT  3, '柳田悠岐',             '0009', '004', '当座', '5013968', 'ﾔﾅｷﾞﾀ ﾕｳｷ'
  UNION ALL SELECT  4, '牧原大成',             '0010', '003', '普通', '9471526', 'ﾏｷﾊﾗ ﾀｲｾｲ'
  UNION ALL SELECT  5, '中村晃',               '0008', '007', '普通', '2857340', 'ﾅｶﾑﾗ ｱｷﾗ'
  UNION ALL SELECT  6, '今宮健太',             '0177', '005', '貯蓄', '6104773', 'ｲﾏﾐﾔ ｹﾝﾀ'
  UNION ALL SELECT  7, '山川穂高',             '0009', '001', '普通', '3390218', 'ﾔﾏｶﾜ ﾎﾀﾞｶ'
  UNION ALL SELECT  8, 'ダウンズ',             '0010', '006', '普通', '7261905', 'ﾀﾞｳﾝｽﾞ'
  UNION ALL SELECT  9, '近藤健介',             '0177', '002', '普通', '1038472', 'ｺﾝﾄﾞｳ ｹﾝｽｹ'
  UNION ALL SELECT 10, 'スチュワート・ジュニア', '0008', '004', '普通', '4820193', 'ｽﾁｭﾜｰﾄ･ｼﾞｭﾆｱ'
       ) v
  JOIN bank_master   b  ON b.bankCode  = v.bankCode
  JOIN branch_master br ON br.bankCode = v.bankCode
                       AND br.branchCode = v.branchCode
 ORDER BY v.seq;

-- 確認：10件、そして銀行と支店の対応がマスタどおりであること
SELECT COUNT(*) AS 件数 FROM payee WHERE userId = 'demo';
SELECT nickname AS 呼び名, bankName AS 金融機関, branchName AS 支店,
       bankAccountType AS 科目, bankAccountNum AS 口座番号, name AS 口座名義
  FROM payee WHERE userId = 'demo' ORDER BY id DESC;
