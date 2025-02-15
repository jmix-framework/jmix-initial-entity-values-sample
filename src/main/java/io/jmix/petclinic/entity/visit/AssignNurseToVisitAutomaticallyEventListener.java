package io.jmix.petclinic.entity.visit;

import io.jmix.core.DataManager;
import io.jmix.core.FetchPlan;
import io.jmix.core.FetchPlanBuilder;
import io.jmix.core.Id;
import io.jmix.core.event.EntityChangedEvent;
import io.jmix.petclinic.EmployeeRepository;
import io.jmix.petclinic.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;


// tag::entity-changed-event-listener-after-commit[]
@Component("petclinic_AssignNurseToVisitAutomatically")
public class AssignNurseToVisitAutomaticallyEventListener {
    private static final Logger log = LoggerFactory.getLogger(AssignNurseToVisitAutomaticallyEventListener.class);

    private final DataManager dataManager;
    private final EmployeeRepository employeeRepository;
    public AssignNurseToVisitAutomaticallyEventListener(DataManager dataManager, EmployeeRepository employeeRepository) {
        this.dataManager = dataManager;
        this.employeeRepository = employeeRepository;
    }

    @TransactionalEventListener // <1>
    @Transactional(propagation = Propagation.REQUIRES_NEW) // <2>
    public void assignNurseToVisitAutomatically(final EntityChangedEvent<Visit> event) {
        try {
            if (event.getType().equals(EntityChangedEvent.Type.CREATED)) { // <3>

                Visit visit = dataManager.load(event.getEntityId()) // <4>
                        .fetchPlan(visitWithAssignedNurse())
                        .one();

                if (visit.getAssignedNurse() != null) {
                    log.info("Nurse already assigned to visit: {}. No automatic assignment needed.", visit.getAssignedNurse());
                    return;
                }

                assignAvailableNurseAutomatically(visit); // <5>
            }
        } catch (Exception e) {
            log.error("Error automatically assigning nurse to Visit: %s".formatted(event.getEntityId()), e);
        }
    }
    // end::entity-changed-event-listener-after-commit[]

    private void assignAvailableNurseAutomatically(Visit visit) {

        List<Visit> overlappingVisits = dataManager.load(Visit.class)
                .query("select e from petclinic_Visit e where e.visitStart < :visitEnd and e.visitEnd > :visitStart")
                .parameter("visitEnd", visit.getVisitEnd())
                .parameter("visitStart", visit.getVisitStart())
                .fetchPlan(visitWithAssignedNurse())
                .joinTransaction(false)
                .list();

        List<User> busyNurses = overlappingVisits.stream()
                .map(Visit::getAssignedNurse)
                .filter(Objects::nonNull)
                .toList();

        Optional<User> availableNurse = employeeRepository.findAllNurses().stream()
                .filter(nurse -> !busyNurses.contains(nurse))
                .findFirst();

        if (availableNurse.isPresent()) {
            log.info("Available nurse found: {}. Assigning Nurse to Visit: {}", availableNurse.get(), visit.getVisitNumber());
            visit.setAssignedNurse(availableNurse.get());
            dataManager.save(visit);
        }
    }

    private static Consumer<FetchPlanBuilder> visitWithAssignedNurse() {
        return v -> {
            v.addFetchPlan(FetchPlan.BASE);
            v.add("assignedNurse", FetchPlan.BASE);
        };
    }
}