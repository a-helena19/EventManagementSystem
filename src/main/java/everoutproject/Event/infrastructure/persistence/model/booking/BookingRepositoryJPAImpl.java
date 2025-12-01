package everoutproject.Event.infrastructure.persistence.model.booking;

import everoutproject.Event.domain.model.booking.BookingRepository;
import everoutproject.Event.infrastructure.mapper.BookingMapper;
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
    public void addNewEvent(everoutproject.Event.domain.model.booking.Booking domainBooking) {
        everoutproject.Event.infrastructure.persistence.model.booking.Booking entity = BookingMapper.toEntity(domainBooking);
        everoutproject.Event.infrastructure.persistence.model.booking.Booking savedEntity = bookingJPARepository.save(entity);

        domainBooking.setId(savedEntity.getId());
    }


    @Override
    public List<everoutproject.Event.domain.model.booking.Booking> findAll() {
        return bookingJPARepository.findAll().stream()
                .map(BookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<everoutproject.Event.domain.model.booking.Booking> findByEventId(Long eventId) {
        return bookingJPARepository.findByEventId(eventId).stream()
                .map(BookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<everoutproject.Event.domain.model.booking.Booking> findByEmail(String email) {
        return bookingJPARepository.findByEmail(email).stream()
                .map(BookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<everoutproject.Event.domain.model.booking.Booking> findByUserId(Long userId) {
        return bookingJPARepository.findByUserId(userId).stream()
                .map(BookingMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void save(everoutproject.Event.domain.model.booking.Booking booking) {
        everoutproject.Event.infrastructure.persistence.model.booking.Booking entity = BookingMapper.toEntity(booking);
        entity.setId(booking.getId());
        bookingJPARepository.save(entity);
    }

}
