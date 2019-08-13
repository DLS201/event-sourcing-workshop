package fr.soat.banking.domain;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static fr.soat.banking.domain.AccountStatus.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertTrue;

public class AccountTest {

    @Test
    public void should_succeed_a_classic_FSM_scenario() {
        // Given
        Account account = Account.create();

        // When
        account.register("toto")
                .deposit(100)
                .deposit(100)
                .withdraw(200)
                .close();

        // Then
        Assertions.assertThat(account.getOwner()).isEqualTo("toto");
        Assertions.assertThat(account.getBalance()).isEqualTo(0);
        Assertions.assertThat(account.getVersion()).isEqualTo(5);
        Assertions.assertThat(account.getStatus()).isEqualTo(CLOSED);
        Assertions.assertThat(account.getChanges())
                .extracting(event -> tuple(event.getClass()))
                .containsExactly(
                        tuple(AccountRegistered.class),
                        tuple(AccountDeposited.class),
                        tuple(AccountDeposited.class),
                        tuple(AccountWithdrawn.class),
                        tuple(AccountClosed.class)
                );

    }

    @Test
    public void should_fail_to_get_negative_balance() {
        // Given
        Account account = Account.create();

        // When
        assertThatThrownBy(() -> account.register("toto")
                .deposit(100)
                .withdraw(200))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessage("Withdrawal of 200 can not be applied with balance of 100");
        assertThat(account.getBalance()).isEqualTo(100);
        assertThat(account.getVersion()).isEqualTo(2);
    }

    @Test
    public void should_fail_with_invalid_decisions_on_new_account() {
        // Given
        Account account = Account.create();
        assertThat(account.getStatus()).isEqualTo(NEW);

        // When
        assertThatThrownBy(() -> account.withdraw(100))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> account.deposit(100))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> account.close())
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void should_fail_with_invalid_decisions_on_open_account() {
        // Given
        Account account = Account.create();
        account.register("alice");
        assertThat(account.getStatus()).isEqualTo(OPEN);

        // When
        assertThatThrownBy(() -> account.register("bob"))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void should_fail_with_invalid_decisions_on_closed_account() {
        // Given
        Account account = Account.create();
        account.register("alice")
                .close();
        assertThat(account.getStatus()).isEqualTo(CLOSED);

        // When
        assertThatThrownBy(() -> account.register("bob"))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> account.withdraw(100))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> account.deposit(100))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> account.close())
                .isInstanceOf(UnsupportedOperationException.class);    }
}
