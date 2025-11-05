package at.fhv.Authors.persistence;


import at.fhv.Authors.domain.model.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventImageRepository extends JpaRepository<EventImage, Long> {
}
