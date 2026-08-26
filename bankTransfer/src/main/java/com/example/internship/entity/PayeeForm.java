package com.example.internship.entity;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

// 振込先を登録するときの呼び名。
// 口座番号だけの一覧は選びにくいので、画面で見分けるための名前を付けてもらう
@Data
public class PayeeForm {

    @NotBlank(message = "呼び名を入力してください")
    //DBがvarchar(20)のため、20文字を超えるとSQLエラーになる
    @Size(max = 20, message = "呼び名は20文字以内で入力してください")
    private String nickname;
}
