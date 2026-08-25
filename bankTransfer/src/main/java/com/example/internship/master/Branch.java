package com.example.internship.master;

// 支店マスタの1行。支店コードは銀行の中でだけ一意なので、銀行コードとセットで扱う
public record Branch(String bankCode, String branchCode, String branchName) {
}
