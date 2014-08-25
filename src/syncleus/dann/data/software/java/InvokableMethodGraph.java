package syncleus.dann.data.software.java;

import syncleus.dann.data.software.java.InvokableMethodGraph.InvokableContext;
import syncleus.dann.graph.DirectedEdge;
import syncleus.dann.graph.MutableDirectedAdjacencyGraph;
import syncleus.dann.graph.SimpleWeightedDirectedEdge;
import syncleus.dann.util.DannError;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.*;

/**
 * Builds a graph of all possible method invocations, given:
 * 1. a set of classes (or package to scan recursively)
 * 2. a set of inputs of a certain type
 */
public class InvokableMethodGraph extends MutableDirectedAdjacencyGraph<InvokableContext, DirectedEdge<InvokableContext>>{
    
    /** the # of types of a set of instances, representing the contents of a potential stack frame */
    public static class StackSlice {
        /** Type -> # of unique instances */
        final Map<Type,Integer> instances = new HashMap();

        public StackSlice(Type oneValue) {
            instances.put(oneValue, 1);
        }
                
        public StackSlice(Method m) {
            //TODO generics
            
            if (!Modifier.isStatic(m.getModifiers())) {
                add(m.getDeclaringClass());
            }

            for (Class<?> pt : m.getParameterTypes()) {
                add(pt);
            }
        }
        
        public StackSlice(StackSlice... s) {
            for (StackSlice x : s) {
                for (Map.Entry<Type, Integer> y : x.instances.entrySet()) {
                    Type yt = y.getKey();
                    int addedCount = y.getValue();
                    if (instances.putIfAbsent(yt, addedCount)!=null) {
                        int count = instances.get(yt);
                        count += addedCount;
                        instances.put(yt, count);                        
                    }
                }
            }
        }

        public void add(Type t) {
            if (instances.putIfAbsent(t, 1)!=null) {
                instances.put(t, instances.get(t) + 1);
            }
        }
        
        public boolean accepts(StackSlice s) {
            //Check that every instance is accounted for
            return (s.instances.keySet().containsAll(instances.keySet()));
        }
        
        
        
        //TODO metrics : specificity (how rare this type is used/reference)

        @Override
        public String toString() {
            return instances.toString();
        }
    }
    
    public static class InvokableContext {
        public final Method m;
        private final StackSlice required;
        private StackSlice produced;

        public InvokableContext(Method m) {
            this.m = m;
            required = new StackSlice(m);

            produced = new StackSlice(m.getReturnType());
            if (!Modifier.isStatic(m.getModifiers()))
                produced.add(m.getDeclaringClass());
        }
        
        public boolean accepts(StackSlice s) {
            //no parameters, don't include
            if (required.instances.size() == 0)
                return false;
            
            //TODO check parametres for m            
            return (required.accepts(s));
        }
        
        public StackSlice produces() {
            return produced;
        }

        @Override
        public String toString() {
            return m.toString() + "{" + required + "|" + produced + "}";
        }
        
        //metrics: afferant calls, efferent calls
        
    }

    public boolean includes(Method m) {        
        if (m.getDeclaringClass().getName().startsWith("java.lang"))
            return false;
        return !m.getDeclaringClass().getName().startsWith("java.util");
    }
    
    public InvokableMethodGraph(Collection<Class<?>> classes) {
        for (Class c : classes) {
            for (Method m : c.getMethods()) {
                if (includes(m))
                    add(new InvokableContext(m));
            }
        }
        
        for (InvokableContext a : getNodes()) {
            for (InvokableContext b : getNodes()) {
                if (a == b) continue;
                if (b.accepts(a.produces())) {
                    System.out.println("Direct: " + a + " -> " + b);
                    add(new SimpleWeightedDirectedEdge<>(a,b,1.0));
                }
            }
        }
    }
    
public final static class ClassFinder {

    private final static char DOT = '.';
    private final static char SLASH = '/';
    private final static String CLASS_SUFFIX = ".class";
    private final static String BAD_PACKAGE_ERROR = "Unable to get resources from path '%s'. Are you sure the given '%s' package exists?";

    public final static List<Class<?>> find(final String scannedPackage) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final String scannedPath = scannedPackage.replace(DOT, SLASH);
        final Enumeration<URL> resources;
        try {
            resources = classLoader.getResources(scannedPath);
        } catch (IOException e) {
            throw new IllegalArgumentException(String.format(BAD_PACKAGE_ERROR, scannedPath, scannedPackage), e);
        }
        final List<Class<?>> classes = new LinkedList<>();
        while (resources.hasMoreElements()) {
            final File file = new File(resources.nextElement().getFile());
            classes.addAll(find(file, scannedPackage));
        }
        return classes;
    }

    private final static List<Class<?>> find(final File file, final String scannedPackage) {
        final List<Class<?>> classes = new LinkedList<>();
        final String resource = scannedPackage + DOT + file.getName();
        if (file.isDirectory()) {
            for (File nestedFile : file.listFiles()) {
                classes.addAll(find(nestedFile, scannedPackage));
            }
        } else if (resource.endsWith(CLASS_SUFFIX)) {
            final int beginIndex = 0;
            final int endIndex = resource.length() - CLASS_SUFFIX.length();
            final String className = resource.substring(beginIndex, endIndex);
            try {
                classes.add(Class.forName(className));
            } catch (ClassNotFoundException ignore) {
            }
        }
        return classes;
    }

}
    
    public static void main(String[] args) throws Exception {
        Package root = DannError.class.getPackage();
        
        //String pkg = "syncleus.dann.util";
        String pkg = "syncleus.dann.data.vector";
        List<Class<?>> classes = ClassFinder.find(pkg);
        
        InvokableMethodGraph g = new InvokableMethodGraph(classes);
        for ( InvokableContext n : g.getNodes()) {
            System.out.println(n);
        }
        System.out.println(g.getNodes().size() + " nodes, " + g.getEdges().size() + " edges");

    }
     
   
}
