package io.jmix.petclinic.view.visit.visit;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.view.EditedEntityContainer;
import io.jmix.flowui.view.StandardDetailView;
import io.jmix.flowui.view.ViewController;
import io.jmix.flowui.view.ViewDescriptor;
import io.jmix.petclinic.entity.visit.Visit;
import io.jmix.petclinic.view.main.MainView;

@Route(value = "regular-checkups/:id", layout = MainView.class)
@ViewController(id = "petclinic_Visit.regularCheckup")
@ViewDescriptor(path = "regular-checkup-detail-view.xml")
@EditedEntityContainer("visitDc")
public class RegularCheckupDetailView extends StandardDetailView<Visit> {
}