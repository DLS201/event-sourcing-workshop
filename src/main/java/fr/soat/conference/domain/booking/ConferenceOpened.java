package fr.soat.conference.domain.booking;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@EqualsAndHashCode
@ToString(callSuper = true)
@Getter
public class ConferenceOpened extends ConferenceEvent {

    private final int places;
    private final int seatPrice;

    public ConferenceOpened(ConferenceName conferenceName, int places, int seatPrice) {
        super(conferenceName);
        this.places = places;
        this.seatPrice = seatPrice;
    }

    @Override
    public void applyOn(Conference conference) {
        conference.apply(this);
    }
}
