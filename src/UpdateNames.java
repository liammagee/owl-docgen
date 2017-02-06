import aterm.ATermAppl;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.mindswap.pellet.taxonomy.Taxonomy;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: liam
 * Date: 13/06/13
 * Time: 11:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class UpdateNames {

    protected static PrintWriter out;
    protected static Taxonomy<ATermAppl> taxonomy;

    public static final String DOMAIN_V1_0_PATH = "data/domain_v1_0/";
    public static final String DOMAIN_V1_1_PATH = "data/domain_v1_1/";
    public static String CURRENT_VERSION_PATH = DOMAIN_V1_1_PATH;


    public static void main(String[]args){
        try {
            updateNames();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }




    public static void updateNames() {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology domainOntology = OntologyRefs.getOSISAOntologyExtended(manager);

            PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(domainOntology);
            reasoner.getKB().realize();

            Set<OWLAnnotationProperty> props =  domainOntology.getAnnotationPropertiesInSignature();
            String hasName = "http://circlesofsustainability.org/ontology#hasName";
            OWLAnnotationProperty hasNameProp = null;
            for (OWLAnnotationProperty prop : props) {
                if (prop.getIRI().toString().equals(hasName))
                    hasNameProp = prop;
            }
            Set<OWLNamedIndividual> individuals = domainOntology.getIndividualsInSignature();
            OWLNamedIndividual i = individuals.iterator().next();
            i.toString();

            String label = "http://www.w3.org/2000/01/rdf-schema#label";
            IRI.create(label);
            OWLDataFactory factory = manager.getOWLDataFactory();
            List<String> names = new ArrayList<String>();
            Set<OWLAnnotationAssertionAxiom> axioms =  domainOntology.getAxioms(AxiomType.ANNOTATION_ASSERTION);
            for (OWLAnnotationAssertionAxiom axiom : axioms) {
                OWLAnnotation a = axiom.getAnnotation();

                OWLLiteral lbl = null;
                if (a.getProperty().getIRI().toString().equals(hasName)) {
                    lbl = factory.getOWLLiteral(a.getValue().toString());
                    OWLAnnotation labelAnnotation =
                            factory.getOWLAnnotation(
                                    factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), lbl);

                    OWLAxiom axiom2 = factory.getOWLAnnotationAssertionAxiom(axiom.getSubject(), labelAnnotation);
                    manager.applyChange(new AddAxiom(domainOntology, axiom2));
                    names.add(axiom.getSubject().toString());
                }
            }
            int axiomCounter = 1;

            for (OWLAnnotationAssertionAxiom axiom : axioms) {
                OWLAnnotation a = axiom.getAnnotation();

                if (! names.contains(axiom.getSubject().toString())) {
                    OWLLiteral lbl = factory.getOWLLiteral("individual-" + axiomCounter++);
                    OWLAnnotation labelAnnotation =
                            factory.getOWLAnnotation(
                                    factory.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI()), lbl);

                    OWLAxiom axiom2 = factory.getOWLAnnotationAssertionAxiom(axiom.getSubject(), labelAnnotation);
                    manager.applyChange(new AddAxiom(domainOntology, axiom2));
                    names.add(axiom.getSubject().toString());
                }
            }

            File mergedFile = new File("data/osis-modified.rdf");
            manager.saveOntology(domainOntology, new FileOutputStream(mergedFile));
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }



















    public static void createReasoner() {
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    }




}
