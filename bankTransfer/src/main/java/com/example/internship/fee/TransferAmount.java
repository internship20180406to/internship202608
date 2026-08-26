package com.example.internship.fee;

// 入力額を「相手が受け取る額」「手数料」「口座から引かれる額」に分けたもの。
//
// 手数料を入力額に含めるかどうかで、どちらが入力額と一致するかが入れ替わる。
//   含めない: 振込額 = 入力額、       引かれる額 = 入力額 + 手数料
//   含める  : 引かれる額 = 入力額、   振込額     = 入力額 - 手数料
public record TransferAmount(int money, int fee, boolean feeIncluded) {

    public int total() {
        return money + fee;
    }

    public static TransferAmount of(int enteredAmount, int fee, boolean feeIncluded) {
        return feeIncluded
                ? new TransferAmount(enteredAmount - fee, fee, true)
                : new TransferAmount(enteredAmount, fee, false);
    }
}
