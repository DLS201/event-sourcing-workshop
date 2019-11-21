package fr.soat.conference.domain.booking;

import fr.soat.conference.domain.order.OrderId;
import fr.soat.eventsourcing.api.AggregateRoot;
import fr.soat.eventsourcing.api.DecisionFunction;
import fr.soat.eventsourcing.api.EvolutionFunction;
import lombok.Getter;

import javax.swing.text.html.Option;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.soat.conference.domain.booking.ConferenceStatus.*;

@Getter
public class Conference extends AggregateRoot<ConferenceName>  {

    private final List<Seat> seats = new ArrayList<>();
    private final List<Seat> availableSeats = new ArrayList<>();
    private int seatPrice;
    private ConferenceStatus status;

    public Conference(ConferenceName conferenceName) {
        super(conferenceName);
        this.status = NEW;
    }

    @DecisionFunction
    public Conference open(int places, int seatPrice) {
        apply(new ConferenceOpened(getId(), places, seatPrice));
        return this;
    }

    @EvolutionFunction
    public void apply(ConferenceOpened conferenceOpened) {
        //FIXME
        // given the input event, init the conference state
        this.status = OPEN;
        this.seatPrice = conferenceOpened.getSeatPrice();

        for (int i = 0; i < conferenceOpened.getPlaces(); i++) {
            Seat seat = new Seat(i);
            this.availableSeats.add(seat);
        }
    }

    @DecisionFunction
    public Optional<Seat> bookSeat(OrderId orderId) {
        //FIXME
        // if some seats are available, we should remove one seat from available seats and return it
        // The possible expected output events are:
        // - SeatBookingRequestRefused
        // - SeatBooked
        if (this.availableSeats.isEmpty()) {
            apply(new SeatBookingRequestRefused(this.getId(), orderId));
            return Optional.empty();
        }

        Seat seat = this.availableSeats.get(0);
        apply(new SeatBooked(this.getId(), orderId, seat));
        return Optional.of(seat);
    }

    @DecisionFunction
    public void cancelBooking(Seat seat) {
        //FIXME
        // The expected output event is:
        // - SeatReleased
        apply(new SeatReleased(this.getId(), seat));
    }

    @EvolutionFunction
    public void apply(SeatBooked conferenceSeatBooked) {
        //FIXME
        // given the input event:
        // - update the remaining available seats
        // - update the conference status if needed
        Seat seat = conferenceSeatBooked.getSeat();
        this.availableSeats.remove(seat);
        this.seats.add(seat);

        if (this.availableSeats.isEmpty()) {
            this.status = FULL;
        }
    }

    @EvolutionFunction
    public void apply(SeatBookingRequestRefused seatBookingRequestRefused) {
        recordChange(seatBookingRequestRefused);
    }

    @EvolutionFunction
    public void apply(SeatReleased seatReleased) {
        //FIXME
        // similar to apply(SeatBooked)
        Seat seat = seatReleased.getSeat();
        this.seats.remove(seat);
        this.availableSeats.add(seat);

        if (this.status == FULL) {
            this.status = OPEN;
        }
    }

    @Override
    public String toString() {
        return "room " + this.getId().getName() +
                ": " +
                availableSeats.size() + " / " + seats.size() + " available seats" +
                " (" + status + ")";
    }

}
