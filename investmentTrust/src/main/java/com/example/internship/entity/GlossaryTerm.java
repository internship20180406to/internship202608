package com.example.internship.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
// 用語集(glossary_term)の1行を表すEntity。閲覧専用の固定データ
public class GlossaryTerm {
    private Long id;
    private Integer sortOrder;
    private String term;
    private String description;
}
