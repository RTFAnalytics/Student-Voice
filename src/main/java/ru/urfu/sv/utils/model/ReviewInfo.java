package ru.urfu.sv.utils.model;

import com.opencsv.bean.CsvBindByPosition;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

@Value
public class ReviewInfo{
    @CsvBindByPosition(position = 0)
    UUID id;
    @CsvBindByPosition(position = 1)
    byte value;
    @CsvBindByPosition(position = 2)
    String studentName;
    @CsvBindByPosition(position = 3)
    String comment;
    @CsvBindByPosition(position = 4)
    String sessionName;
    @CsvBindByPosition(position = 5)
    String courseName;
    @CsvBindByPosition(position = 6)
    String professors;
    @CsvBindByPosition(position = 7)
    String professorName;
    @CsvBindByPosition(position = 8)
    String instituteName;
    @CsvBindByPosition(position = 9)
    String instituteAddress;
    @CsvBindByPosition(position = 10)
    String roomName;
    @CsvBindByPosition(position = 11)
    Instant timestamp;
}
