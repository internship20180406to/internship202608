package com.example.internship.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

// お客様モード(注文履歴確認)のパスワードをハッシュ化・照合するためのBean定義。
// Spring Security本体(認証・認可のフィルタチェーン等)は導入せず、暗号化ユーティリティのみを利用する
@Configuration
public class SecurityBeansConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
