package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
// 金融機関マスタ(institution_master)の1行を表すEntity
public class InstitutionMaster {
    private String institutionCode;
    private String institutionName;
}
