import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.mindswap.pellet.taxonomy.Taxonomy;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.metrics.NumberOfClassesWithMultipleInheritance;
import org.semanticweb.owlapi.metrics.OWLMetric;
import org.semanticweb.owlapi.metrics.OWLMetricManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import aterm.ATermAppl;

/**
 * Created with IntelliJ IDEA.
 * User: liam
 * Date: 13/06/13
 * Time: 11:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class OntologyRefs {

    public static final String DOMAIN_V1_0_PATH = "data/domain_v1_0/";
    public static final String DOMAIN_V1_1_PATH = "data/domain_v1_1/";
    public static String CURRENT_VERSION_PATH = DOMAIN_V1_1_PATH;

    public static OWLOntologyManager getUseCaseOntologyManager() throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology tvUseCasesOntology = getTVOntology(manager);
        OWLOntology radioUseCasesOntology = getRadioOntology(manager);
        return manager;
    }


    public static OWLOntologyManager getModelOntologyManager() throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology domainOntology = getDomainOntology(manager);
        OWLOntology documentOntology = getDocumentOntology(manager);
        return manager;
    }



    public static OWLOntology getOSISAOntologyExtended(OWLOntologyManager manager) {
        return getLocalOntology(manager, "data/osis-modified.rdf");
    }

    public static OWLOntology getOSISAOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, "data/tests/OSIS-DesignA.owl");
    }

    public static OWLOntology getOSISBOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, "data/tests/OSIS-DesignB.owl");
    }

    public static OWLOntology getDocumentOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, "data/document/WcmsDocumentModel.rdf");
    }

    public static OWLOntology getDomainOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, getDomainPath() + "WcmsDomainModel.rdf");
    }

    public static OWLOntology getTVOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, getDomainPath() + "UseCaseTests_TV.owl");
    }

    public static OWLOntology getRadioOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, getDomainPath() + "UseCaseTests_Radio.owl");
    }

    public static OWLOntology getMergedOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, getDomainPath() + "UseCaseTests_merged.owl");
    }


    public static String getDomainPath() {
        return CURRENT_VERSION_PATH;
    }

    public static OWLOntology getLocalOntology(OWLOntologyManager manager, String file) {
        try {
            File ontologyFile = new File(file);
            return manager.loadOntologyFromOntologyDocument(ontologyFile);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

}
