package ru.urfu.sv.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.sv.model.domain.entity.ClassSession;
import ru.urfu.sv.model.domain.entity.Review;
import ru.urfu.sv.model.repository.ReviewRepository;
import ru.urfu.sv.utils.model.ReviewInfo;
import ru.urfu.sv.utils.result.ActionResult;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class ReviewService {

    @Autowired
    private ReviewRepository repository;
    @Autowired
    private ClassSessionService sessionService;
    @Autowired
    private ReviewsReportService reportService;

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

    @Transactional
    public byte[] getReport() throws IOException {

        final List<ReviewInfo> reviewInfoList = reportService.getXlsxReport();

        try (final XSSFWorkbook workbook = new XSSFWorkbook()) {
            final Sheet sheet = workbook.createSheet("Лист1");

            final Row row = sheet.createRow(0);
            int rowNumber = 0;
            row.createCell(rowNumber++).setCellValue("id");
            row.createCell(rowNumber++).setCellValue("review_value");
            row.createCell(rowNumber++).setCellValue("student_name");
            row.createCell(rowNumber++).setCellValue("review_comment");
            row.createCell(rowNumber++).setCellValue("session_name");
            row.createCell(rowNumber++).setCellValue("course_name");
            row.createCell(rowNumber++).setCellValue("professors");
            row.createCell(rowNumber++).setCellValue("professor_name");
            row.createCell(rowNumber++).setCellValue("institute_name");
            row.createCell(rowNumber++).setCellValue("institute_address");
            row.createCell(rowNumber++).setCellValue("room_name");
            row.createCell(rowNumber++).setCellValue("create_timestamp");

            for (final ReviewInfo reviewInfo : reviewInfoList) {

                final Row rowData = sheet.createRow(rowNumber++);
                int cell = 0;
                rowData.createCell(cell++).setCellValue(reviewInfo.getId().toString());
                rowData.createCell(cell++).setCellValue(reviewInfo.getValue());
                rowData.createCell(cell++).setCellValue(reviewInfo.getStudentName());
                rowData.createCell(cell++).setCellValue(reviewInfo.getComment());
                rowData.createCell(cell++).setCellValue(reviewInfo.getSessionName());
                rowData.createCell(cell++).setCellValue(reviewInfo.getCourseName());
                rowData.createCell(cell++).setCellValue(reviewInfo.getProfessors());
                rowData.createCell(cell++).setCellValue(reviewInfo.getProfessorName());
                rowData.createCell(cell++).setCellValue(reviewInfo.getInstituteName());
                rowData.createCell(cell++).setCellValue(reviewInfo.getInstituteAddress());
                rowData.createCell(cell++).setCellValue(reviewInfo.getRoomName());
                rowData.createCell(cell).setCellValue(reviewInfo.getTimestamp().toString());
            }

            try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                workbook.write(stream);
                return stream.toByteArray();
            }
        } catch (IOException e) {
            throw new IOException(e);
        }
    }
}