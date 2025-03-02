package com.now.naaga.place.domain;

import com.now.naaga.place.exception.PlaceException;
import com.now.naaga.place.persistence.repository.PlaceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

import static com.now.naaga.place.exception.PlaceExceptionType.NO_EXIST;

@Transactional
@Service
public class PlaceRecommendService {

    private static final int DISTANCE = 1;

    private final PlaceRepository placeRepository;

    public PlaceRecommendService(final PlaceRepository placeRepository) {
        this.placeRepository = placeRepository;
    }

    public Place recommendRandomPlaceNearBy(final Position position) {
        final List<Place> places = placeRepository.findPlaceByPositionAndDistance(position, DISTANCE);
        if (places.isEmpty()) {
            throw new PlaceException(NO_EXIST);
        }
        return getRandomPlace(places);
    }

    private Place getRandomPlace(final List<Place> places) {
        final Random random = new Random();
        final int randomIndex = random.nextInt(places.size());
        return places.get(randomIndex);
    }
}
