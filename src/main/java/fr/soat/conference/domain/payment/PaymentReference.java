package fr.soat.conference.domain.payment;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@EqualsAndHashCode
public class PaymentReference {

    @Getter
    private final String reference;

    public static PaymentReference generate() {
        return new PaymentReference(UUID.randomUUID().toString());
    }

    public static PaymentReference from(String id) {
        return new PaymentReference(id);
    }

    @Override
    public String toString() {
        return reference;
    }
}
