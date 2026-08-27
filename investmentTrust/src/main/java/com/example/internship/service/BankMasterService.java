package com.example.internship.service;

import com.example.internship.entity.Bank;
import com.example.internship.entity.Branch;
import com.example.internship.repository.BankMasterRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 金融機関マスタ・支店マスタの参照サービス。
 *
 * 今は Repository をそのまま呼んでいるだけだが、Controller から直接 Repository を
 * 呼ばないようにしておくことで、あとから業務ルール（無効な支店を除外する等）を
 * 足すときの置き場所が決まる。
 *
 * @Transactional(readOnly = true):参照しかしないことをDBに伝える指定。
 * 更新用のトランザクションより処理が軽くなり、うっかり更新してしまう事故も防げる。
 */
@Service
@Transactional(readOnly = true)
public class BankMasterService {

    @Autowired
    private BankMasterRepository bankMasterRepository;

    /** 金融機関コードで1件検索する。該当が無ければ空のOptional */
    public Optional<Bank> findBank(String bankCode) {
        return bankMasterRepository.findBank(bankCode);
    }

    /** 金融機関を全件返す */
    public List<Bank> findAllBanks() {
        return bankMasterRepository.findAllBanks();
    }

    /** 支店を1件検索する。金融機関コードとセットで指定する */
    public Optional<Branch> findBranch(String bankCode, String branchCode) {
        return bankMasterRepository.findBranch(bankCode, branchCode);
    }

    /** 指定した金融機関の支店を全件返す */
    public List<Branch> findBranchesByBank(String bankCode) {
        return bankMasterRepository.findBranchesByBank(bankCode);
    }
}
