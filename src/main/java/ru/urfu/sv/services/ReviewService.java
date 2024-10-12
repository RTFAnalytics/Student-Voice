package ru.urfu.sv.services;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.sv.model.domain.entity.ClassSession;
import ru.urfu.sv.model.domain.entity.Review;
import ru.urfu.sv.model.repository.ReviewRepository;
import ru.urfu.sv.utils.excel.ExcelUtil;
import ru.urfu.sv.utils.model.ReviewInfo;
import ru.urfu.sv.utils.result.ActionResult;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ru.urfu.sv.utils.formatters.DateFormatter.DATE_FORMATTER_WITH_HOURS_AND_MINUTES;

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

        try (final SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            final SXSSFSheet sheet = workbook.createSheet("Лист1");

            ExcelUtil.addTitleStyles(workbook);
            final CellStyle titleStyle = workbook.getCellStyleAt(1);

            final Row rowTitle = sheet.createRow(0);
            int cell = 0;
            rowTitle.createCell(cell++).setCellValue("id");
            rowTitle.createCell(cell++).setCellValue("review_value");
            rowTitle.createCell(cell++).setCellValue("student_name");
            rowTitle.createCell(cell++).setCellValue("review_comment");
            rowTitle.createCell(cell++).setCellValue("session_name");
            rowTitle.createCell(cell++).setCellValue("course_name");
            rowTitle.createCell(cell++).setCellValue("professors");
            rowTitle.createCell(cell++).setCellValue("professor_name");
            rowTitle.createCell(cell++).setCellValue("institute_name");
            rowTitle.createCell(cell++).setCellValue("institute_address");
            rowTitle.createCell(cell++).setCellValue("room_name");
            rowTitle.createCell(cell).setCellValue("create_timestamp");

            final int columnLastCellNum = rowTitle.getLastCellNum();

            int rowNumber = 1;
            for (final ReviewInfo reviewInfo : reviewInfoList) {

                final Row rowData = sheet.createRow(rowNumber++);
                int cellData = 0;

                rowData.createCell(cellData++).setCellValue(reviewInfo.getId().toString());
                rowData.createCell(cellData++).setCellValue(reviewInfo.getValue());
                rowData.createCell(cellData++).setCellValue(reviewInfo.getStudentName());
                rowData.createCell(cellData++).setCellValue(reviewInfo.getComment());
                rowData.createCell(cellData++).setCellValue(reviewInfo.getSessionName());
                rowData.createCell(cellData++).setCellValue(reviewInfo.getCourseName());
                rowData.createCell(cellData++).setCellValue(reviewInfo.getProfessors());
                rowData.createCell(cellData++).setCellValue(reviewInfo.getProfessorName());
                rowData.createCell(cellData++).setCellValue(reviewInfo.getInstituteName());
                rowData.createCell(cellData++).setCellValue(reviewInfo.getInstituteAddress());
                rowData.createCell(cellData++).setCellValue(reviewInfo.getRoomName());
                rowData.createCell(cellData).setCellValue(reviewInfo.getTimestamp().atZone(ZoneId.systemDefault()).format(DATE_FORMATTER_WITH_HOURS_AND_MINUTES));
            }

            sheet.trackAllColumnsForAutoSizing();
            sheet.setAutoFilter(CellRangeAddress.valueOf("A" + ":" + "L"));
            for (int i = 0; i < columnLastCellNum; i++) {
                final Cell cellTitle = rowTitle.getCell(i);
                cellTitle.setCellStyle(titleStyle);
                sheet.autoSizeColumn(i);
            }
            sheet.untrackAllColumnsForAutoSizing();

            try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                workbook.write(stream);
                return stream.toByteArray();
            }
        } catch (IOException e) {
            throw new IOException(e);
        }
    }
}