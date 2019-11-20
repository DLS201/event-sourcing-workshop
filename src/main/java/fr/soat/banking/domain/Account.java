package fr.soat.banking.domain;


import fr.soat.eventsourcing.api.AggregateRoot;
import fr.soat.eventsourcing.api.DecisionFunction;
import fr.soat.eventsourcing.api.EvolutionFunction;
import lombok.Getter;

import java.util.UUID;

import static fr.soat.banking.domain.AccountStatus.*;

@Getter
public class Account extends AggregateRoot<AccountId> {

    private String owner;
    private String number;
    private Integer balance = 0;
    private AccountStatus status = NEW;

    public Account(AccountId accountId) {
        super(accountId);
    }

    /* aggregate evolutions  */

    @EvolutionFunction
    void apply(AccountOpened accountOpened) {
        this.owner = accountOpened.getOwner();
        this.number = accountOpened.getNumber();
        this.balance = 0;
        this.status = OPEN;
        recordChange(accountOpened);
    }

    @EvolutionFunction
    void apply(AccountDeposited accountDeposited) {
        this.balance += accountDeposited.getAmount();
        recordChange(accountDeposited);
    }

    @EvolutionFunction
    void apply(AccountWithdrawn accountWithdrawn) {
        this.balance -= accountWithdrawn.getAmount();
        recordChange(accountWithdrawn);
    }

    @EvolutionFunction
    void apply(AccountClosed accountClosed) {
        this.status = CLOSED;
        recordChange(accountClosed);
    }

    /* decisions invoked by commands */

    @DecisionFunction
    public static Account create() {
        return new Account(AccountId.next());
    }

    @DecisionFunction
    public Account open(String owner) {
        if (status != NEW) {
            throw new UnsupportedOperationException("Can not register a " + status + " account");
        }
        AccountOpened event = new AccountOpened(getId(), owner, UUID.randomUUID().toString());
        apply(event);
        return this;
    }

    @DecisionFunction
    public Account deposit(Integer amount) {
        if (status != OPEN) {
            throw new UnsupportedOperationException("Can not deposit on a " + status + " account");
        }
        AccountDeposited event = new AccountDeposited(getId(), amount);
        apply(event);
        return this;
    }

    @DecisionFunction
    public Account withdraw(Integer amount) {
        if (status != OPEN) {
            throw new UnsupportedOperationException("Can not withdraw on a " + status + " account");
        }

        if (amount > balance) {
            throw new InsufficientFundsException("Withdrawal of " + amount + " can not be applied with balance of " + balance);
        }

        AccountWithdrawn event = new AccountWithdrawn(getId(), amount);
        apply(event);
        return this;
    }

    @DecisionFunction
    public Account close() {
        if (status != OPEN) {
            throw new UnsupportedOperationException("Can not close a " + status + " account");
        }

        AccountClosed event = new AccountClosed(getId());
        apply(event);
        return this;
    }

    /* Transfer management */

    @DecisionFunction
    public Account requestTransfer(Account receiverAccount, int amount) {
        if (status != OPEN) {
            throw new UnsupportedOperationException("Can not transfer from a " + status + " account");
        }

        if (amount <= getBalance()) {
            // transfer authorized
            apply(new TransferRequested(getId(), receiverAccount.getId(), amount));
            receiverAccount.credit(this, amount);
        } else {
            //FIXME when funds are insufficient...
            // apply a TransferRequestRefused evolution on sender account
            TransferRequestRefused event = new TransferRequestRefused(getId(), receiverAccount.getId(), amount);
            this.apply(event);
        }

        return this;
    }

    @EvolutionFunction
    void apply(TransferRequested transferRequested) {
        recordChange(transferRequested);
    }

    @EvolutionFunction
    void apply(TransferRequestRefused event) {
        recordChange(event);
    }

    @DecisionFunction
    public void credit(Account senderAccount, int amount) {
        //FIXME expected implementation:
        // IF the receiver account is OPEN
        // 1. apply a FundCredited evolution on receiver account
        // 2. make debit() decition on sender account
        // ELSE
        // 1. apply a CreditRequestRefused evolution on receiver account
        // 2. make abortTransferRequest() decision on sender account
        if (status != OPEN) {
            this.apply(new CreditRequestRefused(getId(), senderAccount.getId(), amount));
            senderAccount.abortTransferRequest(getId(), amount);
        } else {
            this.apply(new FundCredited(getId(), senderAccount.getId(), amount));
            senderAccount.apply(new FundDebited(senderAccount.getId(), getId(), amount));
        }
    }

    @EvolutionFunction
    void apply(FundCredited event) {
        this.balance += event.getAmount();
        recordChange(event);
    }

    @EvolutionFunction
    public void apply(CreditRequestRefused event) {
        recordChange(event);
    }

    @DecisionFunction
    public void abortTransferRequest(AccountId receiverAccountId, int amount ) {
        apply(new TransferRequestAborted(getId(), receiverAccountId, amount));
    }

    @EvolutionFunction
    public void apply(TransferRequestAborted transferRequestAborted) {
        recordChange(transferRequestAborted);
    }

    @DecisionFunction
    public void debit(AccountId receiverAccountId, Integer amount) {
        apply(new FundDebited(getId(), receiverAccountId, amount));
    }

    @EvolutionFunction
    public void apply(FundDebited event) {
        this.balance -= event.getAmount();
        recordChange(event);
    }
}
