package fr.soat.conference.domain.booking;

import fr.soat.conference.domain.order.OrderId;
import fr.soat.conference.infra.booking.ConferenceRepository;
import fr.soat.eventsourcing.impl.InMemoryEventStore;
import fr.soat.eventsourcing.impl.NOOPEventPublisher;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ConferenceTest {

    ConferenceRepository repository = new ConferenceRepository(new InMemoryEventStore(new NOOPEventPublisher()));

    @Test
    public void should_create_book_then_release_seat() {
        Conference picasso = new Conference(ConferenceName.name("picasso dans tous ses etats")).open(3, 10);
        Seat seat1 = picasso.bookSeat(OrderId.next()).get();
        Seat seat2 = picasso.bookSeat(OrderId.next()).get();
        Seat seat3 = picasso.bookSeat(OrderId.next()).get();

        assertThat(picasso.getAvailableSeats()).isEmpty();
        picasso.cancelBooking(seat2);

        repository.save(picasso);
        Conference reloadedPicasso = repository.load(picasso.getId());
        assertThat(picasso.getAvailableSeats()).containsExactly(seat2);
    }

    @Test
    public void should_return_empty_when_booking_with_no_more_places() {
        Conference picasso = new Conference(ConferenceName.name("picasso dans tous ses etats")).open(3, 10);
        Seat seat1 = picasso.bookSeat(OrderId.next()).get();
        Seat seat2 = picasso.bookSeat(OrderId.next()).get();
        Seat seat3 = picasso.bookSeat(OrderId.next()).get();

        assertThat(picasso.bookSeat(OrderId.next())).isEmpty();
    }

}
