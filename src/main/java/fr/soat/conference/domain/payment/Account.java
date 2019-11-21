package fr.soat.conference.domain.payment;

import fr.soat.conference.domain.order.OrderId;
import fr.soat.eventsourcing.api.AggregateRoot;
import fr.soat.eventsourcing.api.DecisionFunction;
import fr.soat.eventsourcing.api.EvolutionFunction;
import lombok.Getter;

@Getter
public class Account extends AggregateRoot<AccountId>  {

    private int balance = 0;

    public Account(AccountId accountId) {
        super(accountId);
    }

    @DecisionFunction
    public Account credit(int amount) {
        //FIXME
        // The expected output event is:
        // - AccountCredited
        apply(new AccountCredited(this.getId(), amount));
        return this;
    }

    @EvolutionFunction
    public void apply(AccountCredited accountCredited) {
        //FIXME
        // should update the balance !
        this.balance += accountCredited.getAmount();
    }

    @DecisionFunction
    public Account requestPayment(int amount, OrderId orderId) {
        //FIXME
        // 1. should always keep trace of request (PaymentRequested event)
        // 2. should then check if funds are sufficient
        // The possible expected output events are:
        // - PaymentAccepted
        // - PaymentRefused
        PaymentRequested paymentRequested = new PaymentRequested(this.getId(), amount);
        recordChange(paymentRequested);

        if (this.balance < amount) {
            apply(new PaymentRefused(this.getId(), amount, orderId));
            return this;
        }

        PaymentReference reference = PaymentReference.generate();
        apply(new PaymentAccepted(reference, this.getId(), amount, orderId));
        return this;
    }

    @EvolutionFunction
    public void apply(PaymentRequested paymentRequested) {
        recordChange(paymentRequested);
    }

    @EvolutionFunction
    public void apply(PaymentAccepted paymentAccepted) {
        //FIXME
        // should update the balance !
        this.balance -= paymentAccepted.getAmount();
    }

    @EvolutionFunction
    public void apply(PaymentRefused paymentRefused) {
        recordChange(paymentRefused);
    }

}
