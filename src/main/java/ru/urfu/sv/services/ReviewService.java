package ru.urfu.sv.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.sv.model.domain.ClassSession;
import ru.urfu.sv.model.domain.Course;
import ru.urfu.sv.model.domain.Institute;
import ru.urfu.sv.model.domain.Review;
import ru.urfu.sv.model.repository.ReviewRepository;
import ru.urfu.sv.utils.result.ActionResult;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {
    private final ReviewRepository repository;
    private final ClassSessionService sessionService;

    @Transactional
    public ActionResult saveReview(Review review) {
        Optional<ClassSession> classSession = sessionService.findSessionById(review.getSessionId());

        if (classSession.isEmpty()) {
            String message = "Пара из отзыва %s не найдена".formatted(review);
            log.warn(message);
            throw new IllegalArgumentException(message);
        }

        Review saved = repository.save(review);
        return repository.existsById(saved.getReviewId()) ? ActionResultFactory.reviewCreated(saved.toString()) : ActionResultFactory.reviewCreatingError();
    }

    public Float getAverageRatingBySessions(List<ClassSession> sessions) {
        List<UUID> sessionsIds = sessions
                .stream()
                .map(ClassSession::getSessionId)
                .toList();

        if(sessionsIds.isEmpty()) return 0f;

        List<Review> reviews = repository.findAllBySessionsIds(sessionsIds);

        Integer sum = 0;
        for (Review review : reviews) {
            sum += review.getValue();
        }

        return reviews.isEmpty() ? 0f : (float) sum / reviews.size();
    }

    public List<Review> findReviewsBySessionId(UUID sessionId) {
        return repository.findAllBySessionId(sessionId);
    }
}
