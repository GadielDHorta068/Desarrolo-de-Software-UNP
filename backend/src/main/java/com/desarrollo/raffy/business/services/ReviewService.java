package com.desarrollo.raffy.business.services;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Review;
import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.business.repository.ReviewRepository;
import com.desarrollo.raffy.business.repository.UserRepository;
import com.desarrollo.raffy.dto.ReviewFromBackToFrontDTO;
import com.desarrollo.raffy.dto.ReviewFromFrontToBackDTO;
import com.desarrollo.raffy.model.User;


import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@Service

@Slf4j

public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ReviewFromBackToFrontDTO> findReviewsByEventCreatorEmail(String creatorEmail) {
        List<Review> reviews = new ArrayList<>();
        reviewRepository.findReviewsByEventCreatorEmail(creatorEmail).forEach(review -> reviews.add(review));
        List<ReviewFromBackToFrontDTO> result = new ArrayList<>();
        for (Review r : reviews) {
            ReviewFromBackToFrontDTO reviewToFront = new ReviewFromBackToFrontDTO();
            reviewToFront.setName(r.getUser().getName());
            reviewToFront.setSurname(r.getUser().getSurname());
            reviewToFront.setEventTitle(r.getEvent().getTitle());
            reviewToFront.setScore(r.getScore());
            reviewToFront.setDelivery(r.getDelivery());
            reviewToFront.setComment(r.getComment());
            result.add(reviewToFront);
        }
        return result;
    }

    @Transactional
    public Review save(ReviewFromFrontToBackDTO aReviewFromFrontToBackDTO, Long aEventId) {
        Optional<User> optionalUser = userRepository.findByEmail(aReviewFromFrontToBackDTO.getEmail());
        Optional<Events> optionalEvent = eventsRepository.findById(aEventId);

        if (optionalUser.isEmpty() || optionalEvent.isEmpty()) {
            throw new IllegalArgumentException("Usuario o evento no encontrados");
        }

        // agregar checkeo de q user sea participante del evento y sea ganador (poss disitnta de cero)
        // hay q modificar los repositorys participante y raffle number para poderbuscar por evento y usuario
        
        Review newReview = new Review();
        newReview.setUser(optionalUser.get());
        newReview.setEvent(optionalEvent.get());
        newReview.setScore(aReviewFromFrontToBackDTO.getScore() / 2);
        newReview.setDelivery(aReviewFromFrontToBackDTO.getDelivery());
        newReview.setComment(aReviewFromFrontToBackDTO.getComment());

        return reviewRepository.save(newReview);
    }
}
