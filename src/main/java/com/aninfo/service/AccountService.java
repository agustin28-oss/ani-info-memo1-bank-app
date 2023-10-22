package com.aninfo.service;

import com.aninfo.exceptions.DepositNegativeSumException;
import com.aninfo.exceptions.InsufficientFundsException;
import com.aninfo.model.Account;
import com.aninfo.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiFunction;

@Service
public class AccountService {

    @FunctionalInterface
    public interface BankAccountPromo{
        void applyPromo(Account account);
    }

    private static class DepositPromo implements BankAccountPromo{
        private final Double MAXCAP = 500.0;
        private Double sum;
        private final float DISCOUNT = 0.10F;
        public DepositPromo(Double sum){
            this.sum = sum;
        }
        @Override
            public void applyPromo(Account account) {
                var currentBalance = account.getBalance();
                if(!account.maxCapReached(MAXCAP) && sum >= 2000) {
                    Double extra = Math.min(this.sum * DISCOUNT, MAXCAP - account.getCap());
                    account.setBalance(currentBalance + extra);
                    account.setCap(account.getCap()+extra);
                }
            }
    }

    @Autowired
    private AccountRepository accountRepository;

    public Account createAccount(Account account) {
        return accountRepository.save(account);
    }

    public Collection<Account> getAccounts() {
        return accountRepository.findAll();
    }

    public Optional<Account> findById(Long cbu) {
        return accountRepository.findById(cbu);
    }

    public void save(Account account) {
        accountRepository.save(account);
    }

    public void deleteById(Long cbu) {
        accountRepository.deleteById(cbu);
    }

    @Transactional
    public Account withdraw(Long cbu, Double sum) {
        Account account = accountRepository.findAccountByCbu(cbu);

        if (account.getBalance() < sum) {
            throw new InsufficientFundsException("Insufficient funds");
        }
        account.setBalance(account.getBalance() - sum);

        accountRepository.save(account);

        return account;
    }

    @Transactional
    public Account deposit(Long cbu, Double sum) {

        if (sum <= 0) {
            throw new DepositNegativeSumException("Cannot deposit negative sums");
        }


        Account account = accountRepository.findAccountByCbu(cbu);
        account.setBalance(account.getBalance() + sum);
        accountRepository.save(account);

        BankAccountPromo promo = new DepositPromo(sum);
        promo.applyPromo(account);

        return account;
    }

}
