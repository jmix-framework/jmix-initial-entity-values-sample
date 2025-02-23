package io.jmix.petclinic.view.visit;

import io.jmix.flowui.component.SupportsTypedValue;
import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.datetimepicker.TypedDateTimePicker;
import io.jmix.petclinic.EmployeeRepository;
import io.jmix.petclinic.entity.NamedEntity;
import io.jmix.petclinic.entity.Person;
import io.jmix.petclinic.entity.User;
import io.jmix.petclinic.entity.pet.Pet;
import io.jmix.petclinic.entity.visit.Visit;

import io.jmix.petclinic.entity.visit.VisitType;
import io.jmix.petclinic.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;


// tag::start-class[]
@Route(value = "visits/:id", layout = MainView.class)
@ViewController("petclinic_Visit.detail")
@ViewDescriptor("visit-detail-view.xml")
@EditedEntityContainer("visitDc")
public class VisitDetailView extends StandardDetailView<Visit> {


    // end::start-class[]
    @Autowired
    private EmployeeRepository employeeRepository;
    @ViewComponent
    private EntityComboBox<User> assignedNurseField;

    @Subscribe
    public void onInit(final InitEvent event) {
        assignedNurseField.setItems(employeeRepository.findAllNurses());
    }


    // tag::init-entity-event[]
    @Subscribe
    public void onInitEntity(final InitEntityEvent<Visit> event) { // <1>
        Visit visit = event.getEntity();
        visit.setType(VisitType.REGULAR_CHECKUP); // <2>
    }

    // end::init-entity-event[]

    // tag::visit-start-value-change-event[]
    @Subscribe("visitStartField") // <1>
    public void onVisitStartFieldTypedValueChange(
            final SupportsTypedValue.TypedValueChangeEvent<TypedDateTimePicker<LocalDateTime>, LocalDateTime> event // <2>
    ) {
        LocalDateTime value = event.getValue();

        if (value != null) {
            getEditedEntity().setVisitEnd(calculateVisitEnd(value)); // <3>
        }
        else {
            getEditedEntity().setVisitEnd(null);
        }
    }

    private LocalDateTime calculateVisitEnd(LocalDateTime visitStart) {

        int durationInMinutes = switch (getEditedEntity().getType()) {
            case REGULAR_CHECKUP -> 30;
            case RECHARGE -> 180;
            case STATUS_CONDITION_HEALING, DISEASE_TREATMENT, OTHER -> 60;
        };

        return visitStart.plusMinutes(durationInMinutes);
    }
    // end::visit-start-value-change-event[]


    // tag::before-save-event[]
    @Subscribe
    public void onBeforeSave(final BeforeSaveEvent event) { // <1>
        if (!StringUtils.hasText(getEditedEntity().getDescription())) { // <2>
            getEditedEntity().setDescription(calculateDefaultDescription(getEditedEntity())); // <3>
        }
    }

    private String calculateDefaultDescription(Visit visit) {
        String petName = Optional.ofNullable(visit.getPet())
                .map(NamedEntity::getName)
                .orElse("the pet");
        String ownerName = Optional.ofNullable(visit.getPet())
                .map(Pet::getOwner)
                .map(Person::getFullName)
                .orElse("the pet owner");

        return switch (visit.getType()) {
            case REGULAR_CHECKUP -> """
                Regular Check-up Notes:
                - Temperature and heart rate measured from %s: Y/N
                - Vaccination status reviewed with %s: Y/N
                - Teeth and gums inspected from %s: Y/N
                - Overall condition recorded: Y/N
                - Follow-up discussion held with %s: Y/N
                """.formatted(petName, ownerName, petName, ownerName);
            case RECHARGE -> """
                Recharge Visit Notes:
                - Fluids and electrolytes replenished for %s: Y/N
                - Supplements provided as needed: Y/N
                - Post-recharge behavior observed in %s: Y/N
                - Recovery instructions shared with %s: Y/N
                """.formatted(petName, petName, ownerName);
            case STATUS_CONDITION_HEALING -> """
                Healing Progress Notes:
                - Healing progress assessed for %s: Y/N
                - Signs of infection or inflammation checked: Y/N
                - Treatment plan adjusted if necessary: Y/N
                - Condition progress discussed with %s: Y/N
                """.formatted(petName, ownerName);
            case DISEASE_TREATMENT -> """
                Disease Treatment Notes:
                - Prescribed medications administered to %s: Y/N
                - Vital signs monitored for %s: Y/N
                - Side effects and patient response recorded: Y/N
                - Treatment outcomes reviewed with %s: Y/N
                """.formatted(petName, petName, ownerName);
            case OTHER -> """
                General Visit Notes:
                - Concerns discussed with %s: Y/N
                - Unusual observations about %s recorded: Y/N
                - Follow-up recommendations provided: Y/N
                """.formatted(ownerName, petName);
        };

    }
    // end::before-save-event[]

// tag::end-class[]
}
// end::end-class[]