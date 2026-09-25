package com.desarrollo.raffy.business.services;

import java.util.List;
import java.util.Optional;
import java.nio.file.AccessDeniedException;
import java.util.ArrayList;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.desarrollo.raffy.model.DeliveryStatus;
import com.desarrollo.raffy.model.Events;
import com.desarrollo.raffy.model.Giveaways;
import com.desarrollo.raffy.model.Raffle;
import com.desarrollo.raffy.model.RegisteredUser;
import com.desarrollo.raffy.model.Review;
import com.desarrollo.raffy.model.Url;
import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.business.repository.ParticipantRepository;
import com.desarrollo.raffy.business.repository.RaffleNumberRepository;
import com.desarrollo.raffy.business.repository.ReviewRepository;
import com.desarrollo.raffy.business.repository.UserRepository;
import com.desarrollo.raffy.dto.ReviewFromBackToFrontDTO;
import com.desarrollo.raffy.dto.ReviewFromFrontToBackDTO;
import com.desarrollo.raffy.exception.NotAllowedToReviewException;
import com.desarrollo.raffy.exception.ResourceNotFoundException;
import com.desarrollo.raffy.exception.ReviewAlreadyExistsException;
import com.desarrollo.raffy.exception.ConflictException;
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
    private ParticipantRepository participantRepository;

    @Autowired
    private RaffleNumberRepository raffleNumberRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UrlService urlService;

    public List<ReviewFromBackToFrontDTO> findReviewsByEventCreatorEmail(String creatorEmail) {
        List<Review> reviews = new ArrayList<>();
        reviewRepository.findReviewsByEventCreatorEmail(creatorEmail).forEach(review -> reviews.add(review));
        List<ReviewFromBackToFrontDTO> result = new ArrayList<>();
        for (Review r : reviews) {
            ReviewFromBackToFrontDTO reviewToFront = new ReviewFromBackToFrontDTO();
            reviewToFront.setName(r.getUser().getName());

            User user = r.getUser();
            if (user instanceof RegisteredUser) {
                // log.warn("El user es de la clase RegisteredUser");
                RegisteredUser regUser = (RegisteredUser) user;
                reviewToFront.setNickname(regUser.getNickname());
            }

            reviewToFront.setSurname(r.getUser().getSurname());
            reviewToFront.setEventId(r.getEvent().getId());
            reviewToFront.setEventTitle(r.getEvent().getTitle());
            reviewToFront.setScore(r.getScore());
            reviewToFront.setDelivery(r.getDelivery());
            reviewToFront.setAwardAlingment(r.getAwardAlingment());
            reviewToFront.setCommunicationRating(r.getCommunicationRating());
            reviewToFront.setComment(r.getComment());
            result.add(reviewToFront);
        }
        return result;
    }

    public Double getAverageScoreByUserEmail(String aUserEmail) {
        Double avgScore = reviewRepository.getReputationOfUserByUserEmail(aUserEmail);
        return (avgScore != null) ? avgScore : 0.0;
    }

    @Transactional
    public Review save(ReviewFromFrontToBackDTO aReviewFromFrontToBackDTO, Long aEventId) {
        Optional<User> optionalUser = userRepository.findByEmail(aReviewFromFrontToBackDTO.getEmail());
        Optional<Events> optionalEvent = eventsRepository.findById(aEventId);

        if (optionalUser.isEmpty() || optionalEvent.isEmpty()) {
            throw new IllegalArgumentException("Usuario o evento no encontrados");
        }

        if (this.reviewRepository.existsByUserAndEvent(optionalUser.get(), optionalEvent.get())) {
            throw new ReviewAlreadyExistsException(
              "Ya enviaste una reseña para este evento."
            );
        }

        List<String> winnersEmails;
        if (optionalEvent.get() instanceof Giveaways) {
            winnersEmails = participantRepository.findWinnerEmailsByEventId(aEventId);
            if (!winnersEmails.contains(optionalUser.get().getEmail())) {
                throw new NotAllowedToReviewException("El usuario que escribe la reseña no es un ganador del evento");
            }
        }
        if (optionalEvent.get() instanceof Raffle) {
            winnersEmails = raffleNumberRepository.findWinnerEmailsByEventId(aEventId);
            if (!winnersEmails.contains(optionalUser.get().getEmail())) {
                throw new NotAllowedToReviewException("El usuario que escribe la reseña no es un ganador de el evento");
            }
        }
        
        Review newReview = new Review();
        newReview.setUser(optionalUser.get());
        newReview.setEvent(optionalEvent.get());
        newReview.setDelivery(aReviewFromFrontToBackDTO.getDelivery());
        newReview.setAwardAlingment(aReviewFromFrontToBackDTO.getAwardAlingment());
        if (aReviewFromFrontToBackDTO.getDelivery() == DeliveryStatus.NO_RECIBIDO) {
            newReview.setAwardAlingment(null);
        }
        newReview.setCommunicationRating(aReviewFromFrontToBackDTO.getCommunicationRating());
        newReview.setScore(aReviewFromFrontToBackDTO.getScore());
        newReview.setComment(aReviewFromFrontToBackDTO.getComment());

        // deshabilitar el link de review

        Url url = urlService.getSingleUseUrlByShortcodeAndEvent(aReviewFromFrontToBackDTO.getUrlShortcode(), aEventId);
        
        if (url == null) {
            throw new ResourceNotFoundException("El enlace de la reseña no es válido.");
        }
        if (Boolean.TRUE.equals(url.getIsUsed())) {
            throw new ConflictException("Este enlace ya fue utilizado.");
        }

        Review savedReview = reviewRepository.save(newReview);

        urlService.markSingleUseUrlAsUsed(url.getShortcode());

        return savedReview;
    }
}
