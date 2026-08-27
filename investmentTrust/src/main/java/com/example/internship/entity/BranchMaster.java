package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
// 支店マスタ(branch_master)の1行を表すEntity。金融機関コードに従属する
public class BranchMaster {
    private String institutionCode;
    private String branchCode;
    private String branchName;
}
