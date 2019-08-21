package fr.soat.banking.domain;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString(callSuper = true)
public class AccountClosed extends AccountEvent {

    public AccountClosed(AccountId accountId) {
        super(accountId);
    }

    @Override
    public void applyOn(Account account) {
        account.on(this);
    }

}
