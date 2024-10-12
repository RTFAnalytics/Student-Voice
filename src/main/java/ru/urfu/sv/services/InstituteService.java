package ru.urfu.sv.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.urfu.sv.model.domain.entity.ClassSession;
import ru.urfu.sv.model.domain.entity.Institute;
import ru.urfu.sv.model.repository.InstituteRepository;
import ru.urfu.sv.utils.result.ActionResult;
import ru.urfu.sv.utils.result.ActionResultFactory;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstituteService {
    private final InstituteRepository repository;

    @Transactional
    public ActionResult createInstitute(String instituteFullName, String instituteShortName, String instituteAddress) {
        Institute institute = repository.save(new Institute(instituteFullName, instituteShortName, instituteAddress));
        return repository.existsById(institute.getInstituteId()) ? ActionResultFactory.instituteCreated() : ActionResultFactory.instituteCreatingError();
    }

    @Transactional
    public void createInstitutesByClassSessions(List<ClassSession> sessions) {
        for (ClassSession session : sessions) {
            String address = session.getCourseDetails().getInstituteAddress();
            if (!repository.existsByAddress(address)) {
                ActionResult result = createInstitute("Не указано", "Не указано", address);
                if (!result.isSuccess()) {
                    log.error("Институт {} не создался - {}", session.getCourseDetails().getCourseName(), result.getFormattedMessage());
                } else {
                    log.info("Создался новый институт - {}", address);
                }
            }
        }
    }

    public Optional<Institute> findInstituteById(Integer instituteId) {
        return repository.findById(instituteId);
    }

    public Optional<Institute> findInstituteByAddress(String address){
        return repository.findByAddressIgnoreCase(address);
    }

    public Optional<Institute> findInstituteByShortName(String instituteShortName) {
        return repository.findByShortNameIgnoreCase(instituteShortName);
    }

    public List<Institute> findAllInstitutes() {
        return repository.findAll();
    }
}
