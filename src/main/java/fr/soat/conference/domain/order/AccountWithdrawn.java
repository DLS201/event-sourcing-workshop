package fr.soat.conference.domain.order;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString(callSuper = true)
public class AccountWithdrawn extends AccountEvent {

    @Getter
    private final Integer amount;

    public AccountWithdrawn(AccountId accountId, Integer amount) {
        super(accountId);
        this.amount = amount;
    }

    @Override
    public void applyOn(Order order) {
        order.apply(this);
    }
}
