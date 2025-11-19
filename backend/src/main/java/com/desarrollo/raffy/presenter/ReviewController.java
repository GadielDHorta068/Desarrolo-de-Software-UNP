package com.desarrollo.raffy.presenter;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.desarrollo.raffy.Response;
import com.desarrollo.raffy.business.services.ReviewService;
import com.desarrollo.raffy.dto.ReviewFromBackToFrontDTO;
import com.desarrollo.raffy.dto.ReviewFromFrontToBackDTO;
import com.desarrollo.raffy.model.Review;

import lombok.extern.slf4j.Slf4j;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/reviews")
@Tag(name = "Reseñas", description = "Gestión de reseñas de eventos: consulta, promedio y creación")

@Slf4j

public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @GetMapping("/user/{userEmail}")
    @Operation(summary = "Reseñas por usuario", description = "Obtiene reseñas asociadas al creador del evento por email")
    public ResponseEntity<?> getReviewsByUserEmail(
        @PathVariable("userEmail") String aUserEmail
    ) {
        try {
            List<ReviewFromBackToFrontDTO> reviews = reviewService.findReviewsByEventCreatorEmail(aUserEmail);
            // log.warn("[reviews] => reviews obtenidos: " + reviews);
            if (reviews.isEmpty() || reviews == null) {
                // return new ResponseEntity<>("no se encontraron reviews para el usuario con email: " + aUserEmail, null);
                return Response.ok(reviews, "No se encontraron reviews para el usuario con email"+aUserEmail);
            }
            return Response.ok(reviews, "Se econtraron "+reviews.size()+" reviews");
        }
        catch (Exception e) {
            return Response.error(null, e.getMessage());
        }

    }

    @GetMapping("/avg-score")
    @Operation(summary = "Promedio de reseñas", description = "Obtiene el puntaje promedio de reseñas para un usuario")
    public ResponseEntity<?> getAvgScoreByUserEmail(
        @RequestParam("email") String aUserEmail) {
        try {
            Double avgScore = reviewService.getAverageScoreByUserEmail(aUserEmail);
            return new ResponseEntity<>(avgScore, HttpStatus.OK);    
        }
        catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
        }
    }

    @PostMapping("/event/{eventId}/create-review")
    @Operation(summary = "Crear reseña", description = "Crea una reseña asociada a un evento")
    public ResponseEntity<?> createReview(
        @PathVariable("eventId") Long aEventId,
        @RequestBody ReviewFromFrontToBackDTO aReviewFromFrontToBack
    ) {
        try {
            Review savedReview = reviewService.save(aReviewFromFrontToBack, aEventId);
            // return Response.ok(savedReview, "Review creada con éxito");
            return Response.ok(null, "Review creada con éxito");
        } catch (IllegalArgumentException e) {
            return Response.error(e, e.getMessage()); // error controlado (400)
        } catch (Exception e) {
            String msgError = "Ocurrió un error inesperado al crear la review";
            if(e.getMessage() != null){
                msgError = e.getMessage();
            }
            return Response.error(null, msgError);
        }
    }

}
