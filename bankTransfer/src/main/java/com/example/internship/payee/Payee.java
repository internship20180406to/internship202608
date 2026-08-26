package com.example.internship.payee;

// 登録された振込先1件。
// 履歴の RecentPayee と項目はほぼ同じだが、こちらは利用者が明示的に登録したもので、
// id と呼び名を持つ。混ぜると「消せる登録先」と「消せない履歴」の区別が付かなくなる
public record Payee(
        int id,
        String nickname,
        String bankCode,
        String bankName,
        String branchCode,
        String branchName,
        String bankAccountType,
        String bankAccountNum,
        String name) {
}
