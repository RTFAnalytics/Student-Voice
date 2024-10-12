package ru.urfu.sv.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.urfu.sv.utils.formatters.CsvFormatter;
import ru.urfu.sv.utils.model.ReviewInfo;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewsReportService {
    @PersistenceContext
    private final EntityManager entityManager;

    public String getCvsReport() {
        List<ReviewInfo> reviewInfos = entityManager.createNamedQuery("findAllReviewInfo", ReviewInfo.class).getResultList();
        return CsvFormatter.toCsv(reviewInfos,
                "id;review_value;student_name;review_comment;session_name;course_name;professors;professor_name;institute_name;institute_address;room_name;create_timestamp\n");
    }

    public List<ReviewInfo> getXlsxReport() {
        return entityManager.createNamedQuery("findAllReviewInfo", ReviewInfo.class).getResultList();
    }
}