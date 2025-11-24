package com.desarrollo.raffy.business.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.desarrollo.raffy.business.repository.EventsRepository;
import com.desarrollo.raffy.business.repository.GuessProgressRepository;
import com.desarrollo.raffy.business.repository.UserRepository;
import com.desarrollo.raffy.dto.GuessProgressDTO;
import com.desarrollo.raffy.dto.UserDTO;
import com.desarrollo.raffy.exception.ResourceNotFoundException;
import com.desarrollo.raffy.dto.GuessCheckResponseDTO;
import com.desarrollo.raffy.model.GuessProgress;
import com.desarrollo.raffy.model.GuessingContest;
import com.desarrollo.raffy.model.GuestUser;
import com.desarrollo.raffy.model.User;

@Service
public class GuessProgressService {

    @Autowired
    private GuessProgressRepository repository;

    @Autowired
    private EventsRepository eventsRepository;

    @Autowired
    private EventsService eventsService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private EvolutionService evolutionService;

    @Value("${evolution.defaultInstance:raffy}")
    private String defaultEvolutionInstance;

    @Transactional
    public GuessProgress register(GuessProgressDTO dto, UserDTO userDto, Long eventId) {

        GuessingContest contest = (GuessingContest) eventsService.getById(eventId);

        // Crear el usuario y persistirlo
        User user = getOrCreateUser(userDto);

        // Crear el progreso
        GuessProgress gp = new GuessProgress();
        gp.setContest(contest);
        gp.setUser(user);
        gp.setAttemptCount(dto.getAttemptCount());
        gp.setNumbersTried(dto.getNumbersTried());
        gp.setAttemptTime(LocalDateTime.now());
        gp.setDurationSeconds(dto.getDurationSeconds());
        gp.setHasWon(dto.isHasWon());

        // Enviar correo de notificación
        emailService.sendGuessProgressEmail(
                user.getEmail(),
                contest.getTitle(),
                user.getName() + " " + user.getSurname(),
                dto.getNumbersTried(),
                gp.getAttemptTime(),
                dto.getAttemptCount(),
                contest.getMaxAttempts(),
                gp.getDurationSeconds());
        return repository.save(gp);
    }

    private User getOrCreateUser(UserDTO userDto) {
        // Buscar usuario existente
        User existing = userService.findByEmail(userDto.getEmail());

        if (existing != null) {
            return existing; // ya existe → se usa tal cual
        }

        // Si no existe, crear un invitado nuevo
        GuestUser user = new GuestUser();
        user.setName(userDto.getName());
        user.setSurname(userDto.getSurname());
        user.setEmail(userDto.getEmail());
        user.setCellphone(userDto.getCellphone());

        return userService.save(user);
    }

    @Transactional(readOnly = true)
    public boolean hasUserWon(Long contestId, Long userId) {
        return repository.existsByContestIdAndUserIdAndHasWonTrue(contestId, userId);
    }

    @Transactional(readOnly = true)
    public GuessCheckResponseDTO hasAlreadyParticipated(Long contestId, String email) {
        GuessingContest contest = (GuessingContest) eventsRepository
                .findById(contestId)
                .orElseThrow(() -> new ResourceNotFoundException("Evento no encontrado"));

        boolean exists = repository.existsByContestIdAndUserEmail(contest.getId(), email);

        GuessCheckResponseDTO dto = new GuessCheckResponseDTO();
        dto.setAlreadyParticipated(exists);
        dto.setMessage(
                exists ? "El usuario ya participó en este concurso." : "El usuario puede participar en este concurso.");
        return dto;
    }

    // Esto va en UserService
    @Transactional
    public User getOrCreateByEmail(UserDTO dto) {
        return userRepository.findByEmail(dto.getEmail())
                .orElseGet(() -> createGuestUser(dto));
    }

    private GuestUser createGuestUser(UserDTO dto) {
        GuestUser user = new GuestUser();
        user.setName(dto.getName());
        user.setSurname(dto.getSurname());
        user.setEmail(dto.getEmail());
        user.setCellphone(dto.getCellphone());

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<GuessProgress> findGuessProgressesByContestId(Long contestId) {

        List<GuessProgress> progresses = repository.findByContestId(contestId);

        if (progresses.isEmpty())
            throw new ResourceNotFoundException("No se encontraron participantes.");

        return progresses;
    }

}
