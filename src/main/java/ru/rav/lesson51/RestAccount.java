package ru.rav.lesson51;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.rav.lesson51.account.create.AccountCreate;
import ru.rav.lesson51.account.create.AccountResult;
import ru.rav.lesson51.account.create.AccountService;

@RestController
public class RestAccount {
    private final AccountService accountService;

    @Autowired
    public RestAccount( AccountService accountService) {
        this.accountService  = accountService;
    }

    @RequestMapping(value = "/corporate-settlement-account/create", method = RequestMethod.POST, produces = "application/json; charset=UTF-8")
    public ResponseEntity<Object> handleAccount(@RequestBody @Valid AccountCreate accountCreate, BindingResult result) {
        AccountResult accountResult = new AccountResult();
        int iError;

        if (result.hasErrors()) {
            for( ObjectError er : result.getAllErrors()) {
                accountResult.setMessage( er.getDefaultMessage());
                return ResponseEntity.status( 400).body( accountResult);
            }
        }

        iError = accountService.setAccount( accountCreate, accountResult);

        if (iError != 200) {
            accountResult.setMessage(accountService.getMessage());
            return ResponseEntity.status(iError).body(accountResult);
        }

        return ResponseEntity.ok( accountResult);
    }
}
