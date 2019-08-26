package fr.soat.conference.domain.booking;

import fr.soat.conference.domain.order.OrderId;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString(callSuper = true)
@Getter
public class SeatBooked extends ConferenceEvent {

    private final OrderId orderId;
    private final Seat bookedSeat;

    public SeatBooked(ConferenceName id, OrderId orderId, Seat bookedSeat) {
        super(id);
        this.orderId = orderId;
        this.bookedSeat = bookedSeat;
    }

    @Override
    public void applyOn(Conference conference) {
        conference.apply(this);
    }
}
