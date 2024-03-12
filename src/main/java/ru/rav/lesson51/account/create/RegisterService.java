package ru.rav.lesson51.account.create;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.rav.lesson51.repo.AccountRepo;
import ru.rav.lesson51.model.Account;
import ru.rav.lesson51.model.TppProduct;
import ru.rav.lesson51.model.TppProductRegister;

@Service
public class RegisterService {
    @Transactional(propagation = Propagation.REQUIRED)
    public void setProduct(TppProduct tppProduct, AccountRepo accountRepo) {
        for (TppProductRegister re : tppProduct.getRegisters())
            if (re.isCheckNew() && (re.getAccount() != null)) {
                Account account = accountRepo.findFirstById(re.getAccount());
                re.setAccountNumber(account.getAccountNumber());
                account.setBussy(true);
                accountRepo.save(account);
            }
    }
}
