package fr.soat.conference.domain.payment;

import fr.soat.conference.domain.order.OrderId;
import fr.soat.conference.infra.payment.AccountRepository;
import fr.soat.eventsourcing.impl.InMemoryEventStore;
import fr.soat.eventsourcing.impl.NOOPEventPublisher;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class AccountTest {

    AccountId myAccountId = AccountId.next();
    AccountRepository repository = new AccountRepository(new InMemoryEventStore(new NOOPEventPublisher()));

    @Test
    public void should_credit_an_account() {
        Account myAccount = new Account(myAccountId);
        myAccount.credit(100);
        repository.save(myAccount);

        myAccount = repository.load(myAccountId);
        myAccount.credit(10);
        repository.save(myAccount);

        myAccount = repository.load(myAccountId);
        assertThat(myAccount.getBalance()).isEqualTo(110);
    }

    @Test
    public void should_pay_with_an_account() {
        Account myAccount = new Account(myAccountId);
        myAccount.credit(100);
        repository.save(myAccount);
        myAccount.requestPayment(10, OrderId.next());
        repository.save(myAccount);

        myAccount = repository.load(myAccountId);
        assertThat(myAccount.getBalance()).isEqualTo(90);
    }

    @Test
    public void should_reject_payment_when_funds_are_insufficient() {
        Account myAccount = new Account(myAccountId);
        myAccount.credit(100);
        repository.save(myAccount);
        myAccount.requestPayment(200, OrderId.next());
        repository.save(myAccount);

        myAccount = repository.load(myAccountId);
        assertThat(myAccount.getBalance()).isEqualTo(100);
    }


}
