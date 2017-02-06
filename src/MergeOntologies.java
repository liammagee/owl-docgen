import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

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
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
public class MergeOntologies {

    protected static PrintWriter out;
    protected static Taxonomy<ATermAppl> taxonomy;

    public static final String DOMAIN_V1_0_PATH = "data/domain_v1_0/";
    public static final String DOMAIN_V1_1_PATH = "data/domain_v1_1/";
    public static String CURRENT_VERSION_PATH = DOMAIN_V1_1_PATH;


    public static void main(String[]args){
        try {
            OWLOntologyManager manager = OntologyRefs.getUseCaseOntologyManager();
            mergeOntologies(manager);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }









    public static void createReasoner() {
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    }




    public static void mergeOntologies(OWLOntologyManager manager) {
        try {
            OWLOntologyMerger merger = new OWLOntologyMerger(manager);
            IRI mergedIRI = IRI.create("http://www.abc.net.au/ontologies/merged-use-cases");
            OWLOntology merged = merger.createMergedOntology(manager, mergedIRI);
            File mergedFile = new File("data/domain/UseCaseTests_merged.owl");
            manager.saveOntology(merged, new FileOutputStream(mergedFile));
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

}
