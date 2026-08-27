package com.example.internship.controller;

import com.example.internship.entity.Bank;
import com.example.internship.entity.Branch;
import com.example.internship.service.BankMasterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 金融機関・支店をコードで引くためのAPI。
 *
 * 画面のJSから fetch で呼び出し、入力されたコードに対応する名称をその場で表示する。
 *
 * ★@Controller ではなく @RestController を使う点に注意。
 *   @Controller … 戻り値の文字列を「テンプレート名」として解釈し、HTMLを返す
 *                 （InvestmentTrustController がこちら）
 *   @RestController … 戻り値のオブジェクトをJSONに変換してそのまま返す
 *
 * ★このAPIは「画面に名称を表示する」ための入力支援であり、
 *   これを通ったからといって値が正しいとは限らない（JSは開発者ツールで無効化できる）。
 *   登録してよいかどうかの最終判断は、これまで通りサーバ側の入力チェックで行う。
 */
@RestController
@RequestMapping("/api/banks")
public class BankMasterApiController {

    @Autowired
    private BankMasterService bankMasterService;

    /**
     * 金融機関の一覧。
     * GET /api/banks
     */
    @GetMapping
    public List<Bank> banks() {
        return bankMasterService.findAllBanks();
    }

    /**
     * 金融機関コードで1件検索する。
     * GET /api/banks/0001  ->  200 {"bankCode":"0001","bankName":"山陰共同銀行",...}
     * 該当が無ければ 404 を返し、JS側は「該当する金融機関がありません」と表示する。
     *
     * ResponseEntity は「HTTPステータス付きの戻り値」を表すクラス。
     * これを使うと、見つかったときは200、無いときは404、と使い分けられる。
     */
    @GetMapping("/{bankCode}")
    public ResponseEntity<Bank> bank(@PathVariable String bankCode) {
        return bankMasterService.findBank(bankCode)
                .map(ResponseEntity::ok)                                //  見つかった -> 200
                .orElseGet(() -> ResponseEntity.notFound().build());    //  無い       -> 404
    }

    /**
     * 指定した金融機関に属する支店の一覧。
     * GET /api/banks/0001/branches
     *
     * 金融機関を選ぶと支店の選択肢が切り替わる、という連動に使う。
     */
    @GetMapping("/{bankCode}/branches")
    public List<Branch> branches(@PathVariable String bankCode) {
        return bankMasterService.findBranchesByBank(bankCode);
    }

    /**
     * 支店コードで1件検索する。
     * GET /api/banks/0001/branches/002
     *
     * URLに金融機関コードを含めているのは、支店コードだけでは支店を特定できないため。
     */
    @GetMapping("/{bankCode}/branches/{branchCode}")
    public ResponseEntity<Branch> branch(@PathVariable String bankCode,
                                         @PathVariable String branchCode) {
        return bankMasterService.findBranch(bankCode, branchCode)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
