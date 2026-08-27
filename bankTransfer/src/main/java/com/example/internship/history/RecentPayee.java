package com.example.internship.history;

import java.time.LocalDate;

// 履歴から拾い出した振込先1件。
// 同じ相手に何度振り込んでいても1件にまとめ、最後に振り込んだ日を添える。
//
// 振込先を決めるのは 金融機関・支店・科目・口座番号 の4つ。
// 口座名義はこの4つが決まれば決まるので、鍵には含めない。
//
// lastAmount は最後に振り込んだ金額。一覧で「いくら送ったか」の目印になる。
// 記録が無い古い行に備えて Integer（null あり）で持つ。
public record RecentPayee(
        String bankCode,
        String bankName,
        String branchCode,
        String branchName,
        String bankAccountType,
        String bankAccountNum,
        String name,
        Integer lastAmount,
        LocalDate lastTransferredOn) {
}
