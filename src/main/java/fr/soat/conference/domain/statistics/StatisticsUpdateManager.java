package fr.soat.conference.domain.statistics;

import fr.soat.conference.domain.booking.Conference;
import fr.soat.conference.domain.booking.ConferenceName;
import fr.soat.conference.domain.booking.SeatBooked;
import fr.soat.conference.domain.booking.SeatReleased;
import fr.soat.conference.domain.order.Order;
import fr.soat.conference.domain.payment.PaymentAccepted;
import fr.soat.conference.infra.booking.ConferenceRepository;
import fr.soat.conference.infra.order.OrderRepository;
import fr.soat.conference.infra.statistics.StatisticsRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class StatisticsUpdateManager {

    private final ConferenceRepository conferenceRepository;
    private final StatisticsRepository statisticsRepository;
    private final OrderRepository orderRepository;

    public StatisticsUpdateManager(ConferenceRepository conferenceRepository, StatisticsRepository statisticsRepository, OrderRepository orderRepository) {
        this.conferenceRepository = conferenceRepository;
        this.statisticsRepository = statisticsRepository;
        this.orderRepository = orderRepository;
    }

    @EventListener
    public void on(PaymentAccepted paymentAccepted) {
        Order order = orderRepository.load(paymentAccepted.getOrderId());
        ConferenceName conferenceName = order.getConferenceName();
        int amount = paymentAccepted.getAmount();
        statisticsRepository.increaseIncomes(conferenceName, amount);
    }

    @EventListener
    public void on(SeatBooked seatBooked) {
        Order order = orderRepository.load(seatBooked.getOrderId());
        ConferenceName conferenceName = order.getConferenceName();
        statisticsRepository.increaseBookingNumber(conferenceName);
    }

    @EventListener
    public void on(SeatReleased seatReleased) {
        Conference conference = conferenceRepository.load(seatReleased.getConferenceName());
        statisticsRepository.decreaseBookingNumber(conference.getId());
    }
}
