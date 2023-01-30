package uk.gov.digital.ho.hocs.queue.migration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MigrationFailureService {

    private final MigrationFailureRepository migrationFailureRepository;


    public MigrationFailureService(MigrationFailureRepository migrationFailureRepository) {
        this.migrationFailureRepository = migrationFailureRepository;
    }

    public void addFailure(MigrationFailure migrationFailure) {
        migrationFailureRepository.save(migrationFailure);
    }
}