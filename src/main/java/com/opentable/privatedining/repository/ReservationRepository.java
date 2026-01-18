package com.opentable.privatedining.repository;

import com.opentable.privatedining.model.Reservation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends MongoRepository<Reservation, ObjectId> {

    @Query("{ 'restaurantId': ?0, 'spaceId': ?1, 'startTime': { $lt: ?3 }, 'endTime': { $gt: ?2 } }")
    List<Reservation> findByRestaurantIdAndSpaceIdAndOverlap(ObjectId restaurantId, UUID spaceId,
        LocalDateTime from, LocalDateTime to);
}