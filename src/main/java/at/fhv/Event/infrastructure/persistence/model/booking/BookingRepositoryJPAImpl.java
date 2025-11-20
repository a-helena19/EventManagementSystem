package at.fhv.Event.infrastructure.persistence.model.booking;

import at.fhv.Event.domain.model.booking.Booking;
import at.fhv.Event.domain.model.booking.BookingRepository;
import at.fhv.Event.infrastructure.mapper.BookingMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class BookingRepositoryJPAImpl implements BookingRepository {

    private final BookingJPARepository bookingJPARepository;

    public BookingRepositoryJPAImpl (BookingJPARepository bookingJPARepository) {
        this.bookingJPARepository = bookingJPARepository;
    }

    @Override
    public void addNewEvent(Booking domainBooking) {
        at.fhv.Event.infrastructure.persistence.model.booking.Booking entity = BookingMapper.toEntity(domainBooking);
        at.fhv.Event.infrastructure.persistence.model.booking.Booking savedEntity = bookingJPARepository.save(entity);

        domainBooking.setId(savedEntity.getId());
    }


    @Override
    public List<Booking> findAll() {
        return bookingJPARepository.findAll().stream()
                .map(BookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Booking> findByEventId(Long eventId) {
        return bookingJPARepository.findByEventId(eventId).stream()
                .map(BookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(Booking booking) {
        at.fhv.Event.infrastructure.persistence.model.booking.Booking entity = BookingMapper.toEntity(booking);
        entity.setId(booking.getId());
        bookingJPARepository.save(entity);
    }

}
