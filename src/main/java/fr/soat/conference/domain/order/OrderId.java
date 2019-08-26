package fr.soat.conference.domain.order;

import fr.soat.eventsourcing.api.AggregateId;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@ToString(of = "value")
public final class OrderId implements AggregateId {

    @Getter
    private final String value;

    public static OrderId next() {
        return new OrderId(String.valueOf(AggregateId.idGenerator.getAndIncrement()));
    }

    public static OrderId from(String id) {
        return new OrderId(id);
    }
}
