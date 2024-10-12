package ru.urfu.sv.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.urfu.sv.model.domain.entity.ClassSession;
import ru.urfu.sv.model.domain.entity.Institute;
import ru.urfu.sv.utils.exceptions.ModeusException;
import ru.urfu.sv.utils.formatters.TemporalFormatter;
import ru.urfu.sv.utils.model.ClassSessionMapper;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ModeusService {
    @Value("${modeus.persons.url}")
    private String personSearchUrl;
    @Value("${modeus.events.url}")
    private String eventsSearchUrl;

    private final WebDriverService webDriverService;
    private final RestTemplate restTemplate;
    private final InstituteService instituteService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public List<ClassSession> getSessionsOfProfessor(String professorFullName, LocalDate dateFrom, LocalDate dateTo) throws ModeusException {
        Optional<String> modeusAuthToken = webDriverService.getModeusAuthToken();
        if (modeusAuthToken.isEmpty()) {
            log.error("Не получилось получить код авторизации Модеус");
            return Collections.emptyList();
        }

        Optional<String> professorModeusId = getProfessorModeusId(professorFullName, modeusAuthToken.get());
        if (professorModeusId.isEmpty()) {
            log.warn("Не получилось найти преподавателя {}", professorFullName);
            return Collections.emptyList();
        }

        Optional<String> eventsJson = getEventsJson(professorModeusId.get(), dateFrom, dateTo, modeusAuthToken.get());
        if (eventsJson.isEmpty() || !ClassSessionMapper.hasEvents(eventsJson.get())) {
            log.warn("Не получилось найти пары для преподавателя {}", professorFullName);
            return Collections.emptyList();
        }

        Map<String, String> institutesAddressNameMap = instituteService.findAllInstitutes()
                .stream().collect(Collectors.toMap(Institute::getAddress, Institute::getShortName));
        return ClassSessionMapper.fromEventsJson(eventsJson.get(), professorFullName, institutesAddressNameMap);
    }

    private Optional<String> getResponseFromModeus(String url, String body, String modeusAuthToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(modeusAuthToken);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode().isError()) {
            log.error("Ошибка при получения данных из Модеуса - {}, {}", response.getStatusCode().value(), response.getBody());
            return Optional.empty();
        }
        return response.getBody() == null ? Optional.empty() : Optional.of(response.getBody());
    }

    private Optional<String> getEventsJson(String professorModeusId, LocalDate dateFrom, LocalDate dateTo, String modeusAuthToken) {
        String body = jsonForEventsSearch(professorModeusId, dateFrom, dateTo.plusDays(1));
        return getResponseFromModeus(eventsSearchUrl, body, modeusAuthToken);
    }

    private String jsonForEventsSearch(String professorModeusId, LocalDate dateFrom, LocalDate dateTo) {
        Map<String, Object> bodyMap = Map.ofEntries(
                Map.entry("size", 500),
                Map.entry("timeMin", TemporalFormatter.formatToOffsetDateTime(dateFrom)),
                Map.entry("timeMax", TemporalFormatter.formatToOffsetDateTime(dateTo)),
                Map.entry("attendeePersonId", Collections.singletonList(professorModeusId))
        );
        try {
            return objectMapper.writeValueAsString(bodyMap);
        } catch (JsonProcessingException e) {
            log.error("Ошибка создания json", e);
            return "";
        }
    }

    private Optional<String> getProfessorModeusId(String professorFullName, String modeusAuthToken) {
        String body = jsonForPersonSearch(professorFullName);
        Optional<String> response = getResponseFromModeus(personSearchUrl, body, modeusAuthToken);

        if (response.isEmpty()) {
            return Optional.empty();
        }

        JsonNode root = null;
        try {
            root = objectMapper.readTree(response.get());
            return Optional.of(root.get("_embedded").get("persons").get(0).get("id").asText());
        } catch (JsonProcessingException | NullPointerException e) {
            log.error("Ошибка во время обработки ответа от модеуса", e);
            return Optional.empty();
        }
    }

    private String jsonForPersonSearch(String fullName) {
        Map<String, Object> bodyMap = Map.ofEntries(
                Map.entry("fullName", fullName),
                Map.entry("sort", "+fullName"),
                Map.entry("size", 2147483647)
        );
        try {
            return objectMapper.writeValueAsString(bodyMap);
        } catch (JsonProcessingException e) {
            log.error("Ошибка создания json", e);
            return "";
        }
    }
}
