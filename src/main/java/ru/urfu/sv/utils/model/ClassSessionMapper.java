package ru.urfu.sv.utils.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.urfu.sv.model.domain.ClassSession;
import ru.urfu.sv.model.domain.CourseDetails;
import ru.urfu.sv.utils.exceptions.ModeusException;
import ru.urfu.sv.utils.formatters.TemporalFormatter;

import java.time.Instant;
import java.util.*;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassSessionMapper {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String DISTANT_SESSION = "Дистант";

    public static List<ClassSession> fromEventsJson(String eventsJson, String professorFullName, Map<String, String> instituteAddressNameMap) throws ModeusException {
        try {
            JsonNode root = mapper.readTree(eventsJson).get("_embedded");
            return getSessions(root, professorFullName, instituteAddressNameMap);
        } catch (Exception e) {
            log.error("Ошибка во время чтения json пар", e);
            throw new ModeusException("Во время обработки ответа от Модеуса произошла ошибка");
        }
    }

    public static boolean hasEvents(String eventsJson) throws ModeusException {
        try {
            JsonNode root = mapper.readTree(eventsJson).get("_embedded");
            return root.get("events") != null && root.get("events").elements().hasNext();
        } catch (Exception e) {
            log.error("Ошибка во время чтения json пар", e);
            throw new ModeusException("Во время обработки ответа от Модеуса произошла ошибка");
        }
    }

    private static List<ClassSession> getSessions(JsonNode root, String professorFullName, Map<String, String> instituteAddressNameMap) {
        Map<UUID, String> coursesNameMap = getCoursesMap(root);
        Map<UUID, FullAddress> addressMap = getAddressMap(root);
        Iterator<JsonNode> eventNodes = root.get("events").elements();
        List<ClassSession> result = new ArrayList<>();
        while (eventNodes.hasNext()) {
            JsonNode eventNode = eventNodes.next();
            ClassSession current = new ClassSession();
            current.setSessionId(UUID.fromString(eventNode.get("id").asText()));
            current.setSessionName(eventNode.get("name").asText());
            current.setStartDateTime(TemporalFormatter.fromLocalDateTimeString(eventNode.get("startsAtLocal").asText()));
            current.setEndDateTime(TemporalFormatter.fromLocalDateTimeString(eventNode.get("endsAtLocal").asText()));
            String status = eventNode.get("holdingStatus").get("name").asText();
            current.setStatus("Неизвестно".equals(status) ? "Запланировано" : status);
            if (eventNode.get("_links").get("course-unit-realization") == null) {
                continue;
            }
            current.setCourseId(UUID.fromString(eventNode.get("_links").get("course-unit-realization").get("href").asText().substring(1)));
            FullAddress address = addressMap.get(current.getSessionId());
            CourseDetails currentCourseDetails = CourseDetails
                    .builder()
                    .courseName(coursesNameMap.get(current.getCourseId()))
                    .instituteName(instituteAddressNameMap.get(address.building()))
                    .instituteAddress(address.building())
                    .professorsNames(professorFullName)
                    .build();
            current.setRoomName(address.room());
            current.setCourseDetails(currentCourseDetails);
            current.setProfessorName(professorFullName);
            current.setCreateTimestamp(Instant.now());
            result.add(current);
        }

        return result;
    }

    private static Map<UUID, String> getCoursesMap(JsonNode root) {
        Map<UUID, String> result = new HashMap<>();
        Iterator<JsonNode> coursesNode = root.get("course-unit-realizations").elements();
        while (coursesNode.hasNext()) {
            JsonNode courseNode = coursesNode.next();
            result.put(UUID.fromString(courseNode.get("id").asText()), courseNode.get("nameShort").asText());
        }
        return result;
    }

    private static Map<UUID, FullAddress> getAddressMap(JsonNode root) {
        Map<String, String> eventRoomsMap = new HashMap<>();
        Iterator<JsonNode> eventRoomsNode = root.get("event-rooms").elements();
        while (eventRoomsNode.hasNext()) {
            JsonNode eventRoomNode = eventRoomsNode.next();
            eventRoomsMap.put(eventRoomNode.get("_links").get("self").get("href").asText(), eventRoomNode.get("_links").get("room").get("href").asText());
        }

        Map<String, FullAddress> roomsMap = new HashMap<>();
        Iterator<JsonNode> roomsNode = root.get("rooms").elements();
        while (roomsNode.hasNext()) {
            JsonNode roomNode = roomsNode.next();
            String room = roomNode.get("name").asText();
            String buildingAddress = roomNode.get("building").get("name").asText();
            roomsMap.put(roomNode.get("_links").get("self").get("href").asText(), new FullAddress(buildingAddress, room));
        }

        Map<UUID, FullAddress> result = new HashMap<>();
        Iterator<JsonNode> locationsNode = root.get("event-locations").elements();
        while (locationsNode.hasNext()) {
            JsonNode locationNode = locationsNode.next();
            FullAddress address;
            if (locationNode.get("_links").get("event-rooms") == null) {
                address = new FullAddress(DISTANT_SESSION, DISTANT_SESSION);
            } else {
                address = roomsMap.get(eventRoomsMap.get(locationNode.get("_links").get("event-rooms").get("href").asText()));
            }
            result.put(UUID.fromString(locationNode.get("eventId").asText()), address);
        }

        return result;
    }

    private record FullAddress(String building, String room) {
    }
}
