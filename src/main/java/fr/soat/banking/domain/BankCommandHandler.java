package fr.soat.banking.domain;

import fr.soat.eventsourcing.api.Command;

public class BankCommandHandler {

    private AccountRepository repository;

    public BankCommandHandler(AccountRepository repository) {
        this.repository = repository;
    }

    @Command
    public AccountId openAccount(String owner) {
        final Account account = Account
                        .create()
                        .register(owner);

        repository.save(account);
        return account.getId();
    }

    @Command
    public Account loadAccount(AccountId id) {
        return repository.load(id);
    }

    @Command
    public void deposit(AccountId id, int amount) {
        //FIXME
        // 1. load the account aggregate using the repository
        // 2. invoke the decision function deposit() on the aggregate to apply the business logic
        // 3. save the mutated aggregate with the repository
        Account account = loadAccount(id);
        account.deposit(amount);

        repository.save(account);
    }

    @Command
    public void withdraw(AccountId id, int amount) {
        //FIXME
        // 1. load the account aggregate using the repository
        // 2. invoke the decision function withdraw() on the aggregate to apply the business logic
        // 3. save the mutated aggregate with the repository
        Account account = loadAccount(id);
        account.withdraw(amount);

        repository.save(account);
    }

    @Command
    public void closeAccount(AccountId id) {
        final Account account = repository.load(id);
        account.close();
        repository.save(account);
    }
}
