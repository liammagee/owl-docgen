import aterm.ATermAppl;
import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.mindswap.pellet.taxonomy.Taxonomy;
import org.mindswap.pellet.taxonomy.TaxonomyNode;
import org.mindswap.pellet.utils.Comparators;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.metrics.NumberOfClassesWithMultipleInheritance;
import org.semanticweb.owlapi.metrics.OWLMetric;
import org.semanticweb.owlapi.metrics.OWLMetricManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.OWLOntologyMerger;

import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: liam
 * Date: 13/06/13
 * Time: 11:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class Main {

    protected static PrintWriter out;
    protected static Taxonomy<ATermAppl> taxonomy;

    public static final String DOMAIN_V1_0_PATH = "data/domain_v1_0/";
    public static final String DOMAIN_V1_1_PATH = "data/domain_v1_1/";
    public static String CURRENT_VERSION_PATH = DOMAIN_V1_1_PATH;


    public static void main(String[]args){
        try {
//            OWLOntologyManager manager = getUseCaseOntologyManager();
//            mergeOntologies(manager);
//            compareMetrics();
            generateDocs();

//            String cmd = args[0];
//            if (cmd.equals("mergeUseCases")) {
//                mergeOntologies();
//            }
//            else {
//                compareMetrics();
//            }
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }




    public static void generateDocs() {
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology domainOntology = getOSISAOntology(manager);

            PelletReasoner reasoner = PelletReasonerFactory.getInstance().createReasoner(domainOntology);
            reasoner.getKB().realize();

            printNodes(reasoner.getTopClassNode(), domainOntology, manager, reasoner);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static void printNodes(Node<OWLClass> node, OWLOntology ontology, OWLOntologyManager manager, OWLReasoner reasoner) {
        Set<OWLClass> entities = node.getEntities();

        Main main = new Main();
        main.generateHtml(entities, ontology, manager, reasoner);
        for (Iterator<OWLClass> iterator = entities.iterator(); iterator.hasNext(); ) {
            OWLClass next = iterator.next();
            printOwlClass(next, ontology, manager, reasoner, 0);
        }
    }

    public static StringBuffer printOwlClass(OWLClass owlClass, OWLOntology ontology, OWLOntologyManager manager, OWLReasoner reasoner, int depth) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < depth; i++) {
            buffer.append("   ");
        }
        String name = owlClass.getIRI().getFragment();

        buffer.append(name);
        String label = null;

        Set<OWLOntology> ontologies = manager.getOntologies();
        for (Iterator<OWLOntology> iterator = ontologies.iterator(); iterator.hasNext(); ) {
            OWLOntology localOntology = iterator.next();
            Set<OWLAnnotation> annotations = owlClass.getAnnotations(localOntology);
            for (Iterator<OWLAnnotation> annotationIterator = annotations.iterator(); annotationIterator.hasNext(); ) {
                OWLAnnotation next = annotationIterator.next();
                if (next.getProperty().getIRI().getFragment().equals("label"))
                    label = next.getValue().toString();

                try {
                    buffer.append("; ");
                    buffer.append(next.getProperty().getIRI().getFragment());
                    buffer.append(" = ");
                    buffer.append(((OWLLiteral)next.getValue()).getLiteral());
                }
                catch (ClassCastException cce) {
                    System.out.println("Annotation value is a not a literal.");
                }
            }
        }
        if (label != null)
            buffer.append("; label = " + label);
        System.out.println(buffer.toString());


        Set<OWLClass> subClasses = reasoner.getSubClasses(owlClass, true).getFlattened();
        for (Iterator<OWLClass> owlClassIterator = subClasses.iterator(); owlClassIterator.hasNext(); ) {
            OWLClass childClass = owlClassIterator.next();


            if (!reasoner.getBottomClassNode().getEntities().contains(childClass))
                printOwlClass(childClass, ontology, manager, reasoner, depth + 1);
        }

        return buffer;
    }

    public String trim(String str) {
        str = str.replace("'", "\"");
        return str.trim()/*.replace("\n", "").replace("\r", "")*/;
    }

    public Set<OWLClass> sortClasses(Set<OWLClass> classes) {
        Set<OWLClass> sortedClasses = new TreeSet<OWLClass>(new Comparator<OWLClass>() {
            @Override
            public int compare(OWLClass owlClass, OWLClass owlClass2) {
                String fragment1 = owlClass.getIRI().getFragment();
                String fragment2 = owlClass2.getIRI().getFragment();
                return fragment1.compareTo(fragment2);
            }
        });
        sortedClasses.addAll(classes);
        return sortedClasses;
    }

    public Map<OWLClass, Set<OWLDataProperty>>  getDataProperties(OWLOntologyManager manager, OWLReasoner reasoner) {
        Node<OWLDataProperty> topDataPropertyNode = reasoner.getTopDataPropertyNode();
        Set<OWLDataProperty> entities = topDataPropertyNode.getEntities();
        Map<OWLClass, Set<OWLDataProperty>> map = new HashMap<OWLClass, Set<OWLDataProperty>>();
        for (Iterator<OWLDataProperty> iterator = entities.iterator(); iterator.hasNext(); ) {
            OWLDataProperty owlDataProperty = iterator.next();
            getChildDataProperties(owlDataProperty, map, manager, reasoner);
            owlDataProperty.getDomains(manager.getOntologies());
        }
        return map;
     }

    public void getChildDataProperties(OWLDataProperty owlDataProperty, Map<OWLClass, Set<OWLDataProperty>>  map, OWLOntologyManager manager, OWLReasoner reasoner) {
        Set<OWLClass> dataPropertyDomains = reasoner.getDataPropertyDomains(owlDataProperty, true).getFlattened();
        for (Iterator<OWLClass> owlClassIterator = dataPropertyDomains.iterator(); owlClassIterator.hasNext(); ) {
            OWLClass owlClass = owlClassIterator.next();
            Set<OWLDataProperty> dataPropertySet = map.get(owlClass);
            if (dataPropertySet == null) {
                dataPropertySet = new TreeSet<OWLDataProperty>();
                map.put(owlClass, dataPropertySet);
            }
            dataPropertySet.add(owlDataProperty);
        }

        Set<OWLDataProperty> subDataProperties = reasoner.getSubDataProperties(owlDataProperty, true).getFlattened();
        for (Iterator<OWLDataProperty> owlDataPropertyIterator = subDataProperties.iterator(); owlDataPropertyIterator.hasNext(); ) {
            OWLDataProperty childDataProperty = owlDataPropertyIterator.next();
            getChildDataProperties(childDataProperty, map, manager, reasoner);
        }
    }

    public Map<OWLClass, Set<OWLObjectProperty>>  getObjectProperties(OWLOntologyManager manager, OWLReasoner reasoner) {
        Node<OWLObjectPropertyExpression> topDataPropertyNode = reasoner.getTopObjectPropertyNode();
        Set<OWLObjectPropertyExpression> entities = topDataPropertyNode.getEntities();
        Map<OWLClass, Set<OWLObjectProperty>> map = new HashMap<OWLClass, Set<OWLObjectProperty>>();
        for (Iterator<OWLObjectPropertyExpression> iterator = entities.iterator(); iterator.hasNext(); ) {
            OWLObjectPropertyExpression objectPropertyExpression = iterator.next();
            getChildObjectProperties(objectPropertyExpression.getNamedProperty(), map, manager, reasoner);
            objectPropertyExpression.getDomains(manager.getOntologies());
        }
        return map;
     }

    public void getChildObjectProperties(OWLObjectProperty owlObjectProperty, Map<OWLClass, Set<OWLObjectProperty>>  map, OWLOntologyManager manager, OWLReasoner reasoner) {
        Set<OWLClass> objectPropertyDomains = reasoner.getObjectPropertyDomains(owlObjectProperty, true).getFlattened();
        for (Iterator<OWLClass> owlClassIterator = objectPropertyDomains.iterator(); owlClassIterator.hasNext(); ) {
            OWLClass owlClass = owlClassIterator.next();
            Set<OWLObjectProperty> objectPropertySet = map.get(owlClass);
            if (objectPropertySet == null) {
                objectPropertySet = new TreeSet<OWLObjectProperty>();
                map.put(owlClass, objectPropertySet);
            }
            objectPropertySet.add(owlObjectProperty);

        }

        Set<OWLObjectPropertyExpression> subObjectProperties = reasoner.getSubObjectProperties(owlObjectProperty, true).getFlattened();
        for (Iterator<OWLObjectPropertyExpression> owlObjectPropertyIterator = subObjectProperties.iterator(); owlObjectPropertyIterator.hasNext(); ) {
            OWLObjectPropertyExpression childObjectProperty = owlObjectPropertyIterator.next();
            getChildObjectProperties(childObjectProperty.getNamedProperty(), map, manager, reasoner);
        }
    }

    public void generateHtml(Set<OWLClass> rootClasses, OWLOntology ontology, OWLOntologyManager manager, OWLReasoner reasoner) {
        try {
            String docsPath = "docs/";
            String templatePath = docsPath + "template/";
            Velocity.init(templatePath + "velocity.properties");
            String templateFile =  templatePath + "ontology.vm";
            Map<OWLClass, Set<OWLDataProperty>> dataProperties = getDataProperties(manager, reasoner);
            Map<OWLClass, Set<OWLObjectProperty>> objectProperties = getObjectProperties(manager, reasoner);


            VelocityContext context = new VelocityContext();
            context.put("roots", rootClasses);
            context.put("manager", manager);
            context.put("reasoner", reasoner);
            context.put("namespace", ontology.getOntologyID().getOntologyIRI().toString() + "#");
            context.put("dataProperties", dataProperties);
            context.put("objectProperties", objectProperties);
            context.put("trimmer", this);
            Template template =  null;

            try {
                template = Velocity.getTemplate(templateFile);
            }
            catch( ResourceNotFoundException rnfe ) {
                System.out.println("Example : error : cannot find template " + templateFile );
            }
            catch( ParseErrorException pee) {
                System.out.println("Example : Syntax error in template " + templateFile + ":" + pee );
            }
            FileWriter fileWriter = new FileWriter(docsPath + "domain_model.html");
            BufferedWriter writer = writer = new BufferedWriter(fileWriter);
//            BufferedWriter writer = writer = new BufferedWriter(new OutputStreamWriter(System.out));

            if ( template != null)
                template.merge(context, writer);

            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }


    public static void createReasoner() {
        OWLReasonerFactory reasonerFactory = new StructuralReasonerFactory();
    }

    public static void compareMetrics() {
        try {
            OWLOntologyManager manager = getModelOntologyManager();
            List<OWLMetric<?>> metrics = new ArrayList<OWLMetric<?>>();
            NumberOfClassesWithMultipleInheritance m1 = new NumberOfClassesWithMultipleInheritance(manager);
            m1.setImportsClosureUsed(true);
            metrics.add(m1);
            OWLMetricManager metricManager = new OWLMetricManager(metrics);
            metricManager.setOntology(manager.getOntology(IRI.create("file:/Users/liam/Dropbox/work/abc/2013%20ABC%20Domain%20Modelling/code/data/domain/WcmsDomainModel.rdf")));
            System.out.println("metricManager = " + metricManager.toString());
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void getRelevantMetrics() {

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

    private static OWLOntologyManager getUseCaseOntologyManager() throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology tvUseCasesOntology = getTVOntology(manager);
        OWLOntology radioUseCasesOntology = getRadioOntology(manager);
        return manager;
    }


    private static OWLOntologyManager getModelOntologyManager() throws OWLOntologyCreationException {
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology domainOntology = getDomainOntology(manager);
        OWLOntology documentOntology = getDocumentOntology(manager);
        return manager;
    }



    private static OWLOntology getOSISAOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, "data/OSIS-DesignA.owl");
    }

    private static OWLOntology getOSISBOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, "data/OSIS-DesignB.owl");
    }

    private static OWLOntology getDocumentOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, "data/document/WcmsDocumentModel.rdf");
    }

    private static OWLOntology getDomainOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, getDomainPath() + "WcmsDomainModel.rdf");
    }

    private static OWLOntology getTVOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, getDomainPath() + "UseCaseTests_TV.owl");
    }

    private static OWLOntology getRadioOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, getDomainPath() + "UseCaseTests_Radio.owl");
    }

    private static OWLOntology getMergedOntology(OWLOntologyManager manager) {
        return getLocalOntology(manager, getDomainPath() + "UseCaseTests_merged.owl");
    }


    private static String getDomainPath() {
        return CURRENT_VERSION_PATH;
    }

    private static OWLOntology getLocalOntology(OWLOntologyManager manager, String file) {
        try {
            File ontologyFile = new File(file);
            return manager.loadOntologyFromOntologyDocument(ontologyFile);
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        }
    }

}
