package com.example.internship.master;

// 検索候補として画面へ返す形。
// 金融機関と支店で項目名が違う（bankCode / branchCode）ままJSONにすると
// 画面側の処理を2種類書くことになるので、ここで共通の形に揃える。
public record Suggestion(String code, String name) {

    public static Suggestion of(Bank bank) {
        return new Suggestion(bank.bankCode(), bank.bankName());
    }

    public static Suggestion of(Branch branch) {
        return new Suggestion(branch.branchCode(), branch.branchName());
    }
}
