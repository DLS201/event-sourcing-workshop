package fr.soat.conference.domain.order;


import fr.soat.conference.domain.booking.ConferenceName;
import fr.soat.conference.domain.booking.Seat;
import fr.soat.conference.domain.booking.SeatReleased;
import fr.soat.conference.domain.payment.AccountId;
import fr.soat.conference.domain.payment.PaymentReference;
import fr.soat.eventsourcing.api.AggregateRoot;
import fr.soat.eventsourcing.api.DecisionFunction;
import fr.soat.eventsourcing.api.EvolutionFunction;
import lombok.Getter;
import lombok.ToString;

import static fr.soat.conference.domain.order.OrderStatus.*;

@Getter
@ToString(callSuper = true, of = {"status", "conferenceName", "accoundId", "seat" })
public class Order extends AggregateRoot<OrderId> {

    private OrderStatus status;
    private ConferenceName conferenceName;
    private AccountId accountId;

    private Seat seat;
    private PaymentReference paymentReference;

    public Order(OrderId orderId) {
        super(orderId);
        this.status = NEW;
    }

    @DecisionFunction
    public Order requestBooking(ConferenceName conferenceName, AccountId accountForPayment) {
        OrderRequested event = new OrderRequested(getId(), conferenceName, accountForPayment);
        apply(event);
        return this;
    }

    @EvolutionFunction
    void apply(OrderRequested orderRequested) {
        //FIXME
        // should init the state of order (accountId, conferenceName)
        this.accountId = orderRequested.getAccountId();
        this.conferenceName = orderRequested.getConferenceName();

        recordChange(orderRequested);
    }

    @DecisionFunction
    public Order assign(Seat bookedSeat) {
        //FIXME
        //  expected output event is:
        // - OrderSeatBooked
        apply(new OrderSeatBooked(this.getId(), bookedSeat));
        return this;
    }

    @EvolutionFunction
    public void apply(OrderSeatBooked orderSeatBooked) {
        //FIXME
        // should update state (order status and assigned seat)
        this.seat = orderSeatBooked.getBookedSeat();
        this.status = SEAT_BOOKED;

        recordChange(orderSeatBooked);
    }

    @DecisionFunction
    public void failSeatBooking() {
        //FIXME
        //  expected output event is:
        // - OrderSeatBookingFailed
        apply(new OrderSeatBookingFailed(this.getId()));
    }

    @EvolutionFunction
    void apply(OrderSeatBookingFailed orderSeatBookingFailed) {
        //FIXME
        // should update state:
        // - order status
        // - (no) assigned seat
        this.seat = null;
        this.status = SEAT_BOOKING_FAILED;

        recordChange(orderSeatBookingFailed);
    }

    @DecisionFunction
    public void confirmPayment(PaymentReference paymentReference) {
        //FIXME
        //  expected output event is:
        // - OrderPaid
        apply(new OrderPaid(this.getId(), paymentReference));
    }

    @EvolutionFunction
    void apply(OrderPaid orderPaid) {
        //FIXME
        // should update state:
        // - order status
        // - the payment reference
        this.paymentReference = orderPaid.getPaymentReference();
        this.status = PAID;

        recordChange(orderPaid);
    }

    @DecisionFunction
    public void refusePayment() {
        //FIXME
        //  expected output event is:
        // - OrderPaymentRefused
        apply(new OrderPaymentRefused(this.getId()));
    }

    @EvolutionFunction
    void apply(OrderPaymentRefused orderPaymentRefused) {
        //FIXME
        // should update state:
        // - order status
        // - (no) payment reference
        // - but also the fact the is NO more assigned seat !
        this.paymentReference = null;
        this.status = PAYMENT_REFUSED;
        this.seat = null;

        recordChange(orderPaymentRefused);
    }

}
