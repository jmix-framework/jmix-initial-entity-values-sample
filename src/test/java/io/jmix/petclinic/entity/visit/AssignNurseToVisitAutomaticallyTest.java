package io.jmix.petclinic.entity.visit;

import io.jmix.core.DataManager;
import io.jmix.core.Id;
import io.jmix.petclinic.EmployeeRepository;
import io.jmix.petclinic.entity.User;
import io.jmix.petclinic.entity.pet.Pet;
import io.jmix.petclinic.test_support.AuthenticatedAsAdmin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for AssignNurseToVisitAutomaticallyEventListener
 */
@SpringBootTest
@ExtendWith(AuthenticatedAsAdmin.class)
public class AssignNurseToVisitAutomaticallyTest {

    @Autowired
    DataManager dataManager;

    List<Visit> createdVisits = new ArrayList<>();
    @Autowired
    private EmployeeRepository employeeRepository;
    private User comfey;
    private User joy;
    private LocalDateTime visitDate;

    @BeforeEach
    void setUp() {
        List<User> allNurses = employeeRepository.findAllNurses();
        joy = findNurseByUsername(allNurses, "joy");
        comfey = findNurseByUsername(allNurses, "comfey");
        visitDate = LocalDateTime.now().plusDays(new Random().nextLong(10000, 100000));
    }

    private static User findNurseByUsername(List<User> allNurses, String username) {
        return allNurses.stream().filter(it -> it.getUsername().equals(username)).findFirst().orElseThrow();
    }

    @Test
    void test_nurseAssignmentOnOverlappingVisits() {

        // given: Create Visit 1 with Nurse Joy (13:00 - 13:30)
        Visit visit1 = createVisit(
                visitDate.withHour(13).withMinute(0),
                visitDate.withHour(13).withMinute(30),
                joy);
        dataManager.save(visit1);

        // when: Create Visit 2 (13:15 - 13:45) without assigned Nurse
        Visit visit2 = createVisit(
                visitDate.withHour(13).withMinute(15),
                visitDate.withHour(13).withMinute(45),
                null);
        dataManager.save(visit2);

        // then:
        Visit loadedVisit2 = dataManager.load(Visit.class).id(visit2.getId()).one();
        assertThat(loadedVisit2.getAssignedNurse()).isEqualTo(comfey);
    }

    @Test
    void test_noNurseAvailable_noAssignmentOccurs() {
        // Given: Joy and Comfey are both booked from 13:00 to 13:30
        Visit visit1 = createVisit(
                visitDate.withHour(13).withMinute(0),
                visitDate.withHour(13).withMinute(30),
                joy);
        dataManager.save(visit1);

        Visit visit2 = createVisit(
                visitDate.withHour(13).withMinute(0),
                visitDate.withHour(13).withMinute(30),
                comfey);
        dataManager.save(visit2);

        // When: A new visit (13:15 - 13:45) is created without an assigned nurse
        Visit visit3 = createVisit(
                visitDate.withHour(13).withMinute(15),
                visitDate.withHour(13).withMinute(45),
                null);
        dataManager.save(visit3);

        // Then: No nurse is assigned because both are occupied
        Visit loadedVisit3 = dataManager.load(Visit.class).id(visit3.getId()).one();
        assertThat(loadedVisit3.getAssignedNurse()).isNull();
    }


    @Test
    void test_bothNursesAvailable_firstOneSelected() {
        // When: A new visit (13:00 - 13:30) is created without an assigned nurse
        Visit visit = createVisit(
                visitDate.withHour(13).withMinute(0),
                visitDate.withHour(13).withMinute(30),
                null);
        dataManager.save(visit);

        // Then: The first available nurse is assigned
        Visit loadedVisit = dataManager.load(Visit.class).id(visit.getId()).one();
        assertThat(loadedVisit.getAssignedNurse()).isIn(List.of(comfey, joy));
    }

    @Test
    void test_backToBackVisits_otherNurseAssigned() {
        // Given: Joy is booked from 13:00 to 13:30
        Visit visit1 = createVisit(
                visitDate.withHour(13).withMinute(0),
                visitDate.withHour(13).withMinute(30),
                joy);
        dataManager.save(visit1);

        // When: A new visit starts immediately after (13:30 - 14:00) without a nurse
        Visit visit2 = createVisit(
                visitDate.withHour(13).withMinute(30),
                visitDate.withHour(14).withMinute(0),
                null);
        dataManager.save(visit2);

        // Then: Comfey is assigned to give nurses a little pause
        Visit loadedVisit2 = dataManager.load(Visit.class).id(visit2.getId()).one();
        assertThat(loadedVisit2.getAssignedNurse()).isEqualTo(comfey);
    }


    @Test
    void test_shortVisitWithinLongVisit_otherNurseAssigned() {
        // Given: Joy has a long visit from 13:00 to 14:00
        Visit longVisit = createVisit(
                visitDate.withHour(13).withMinute(0),
                visitDate.withHour(14).withMinute(0),
                joy);
        dataManager.save(longVisit);

        // When: A short visit (13:15 - 13:30) is created without an assigned nurse
        Visit shortVisit = createVisit(
                visitDate.withHour(13).withMinute(15),
                visitDate.withHour(13).withMinute(30),
                null);
        dataManager.save(shortVisit);

        // Then: Comfey is assigned because Joy is occupied
        Visit loadedShortVisit = dataManager.load(Visit.class).id(shortVisit.getId()).one();
        assertThat(loadedShortVisit.getAssignedNurse()).isEqualTo(comfey);
    }

    private Visit createVisit(LocalDateTime start, LocalDateTime end, User nurse) {
        Visit visit = dataManager.create(Visit.class);
        visit.setVisitStart(start);
        visit.setVisitEnd(end);
        visit.setType(VisitType.REGULAR_CHECKUP);
        visit.setPet(dataManager.load(Pet.class).all().one());
        visit.setTreatmentStatus(VisitTreatmentStatus.UPCOMING);
        visit.setAssignedNurse(nurse);
        createdVisits.add(visit);
        return visit;
    }

    @AfterEach
    void tearDown() {
        createdVisits.forEach(it -> dataManager.remove(Id.of(it)));
    }
}