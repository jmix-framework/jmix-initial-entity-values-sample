package io.jmix.petclinic.view.visit;

import io.jmix.flowui.component.combobox.EntityComboBox;
import io.jmix.flowui.component.valuepicker.EntityPicker;
import io.jmix.petclinic.EmployeeRepository;
import io.jmix.petclinic.entity.User;
import io.jmix.petclinic.entity.visit.Visit;

import io.jmix.petclinic.entity.visit.VisitType;
import io.jmix.petclinic.view.main.MainView;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.*;
import org.springframework.beans.factory.annotation.Autowired;


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

// tag::end-class[]
}
// end::end-class[]