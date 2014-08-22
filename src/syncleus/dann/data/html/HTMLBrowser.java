package syncleus.dann.data.html;

import java.io.IOException;
import java.util.ArrayList;
import javafx.application.Application;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import netscape.javascript.JSObject;
import org.w3c.dom.Document;

 
public class HTMLBrowser extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        StackPane root = new StackPane();
 
        WebView view = new WebView();
        WebEngine engine = view.getEngine();
        engine.load("http://localhost:8080/");
        root.getChildren().add(view);
        
        Scene scene = new Scene(root, 800, 600);
        stage.setScene(scene);
        stage.show();
        
        
          engine.getLoadWorker().stateProperty().addListener(new javafx.beans.value.ChangeListener<State>() {
                public void changed(ObservableValue ov, State oldState, State newState) {
                    if (newState == Worker.State.SUCCEEDED) {
                        
                        /*
                        Document doc = engine.getDocument();
                        try {
                            Transformer transformer = TransformerFactory.newInstance().newTransformer();
                            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
                            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
                            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
                            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
                            

                            transformer.transform(new DOMSource(doc),
                                    new StreamResult(new OutputStreamWriter(System.out, "UTF-8")));
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }*/


                            String detect = 
                            "function deepText(node){"+
                                "var A= [];"+
                                "if(node){"+
                                "node= node.firstChild;"+
                                "while(node!= null){"+
                                "if ((node.nodeType== 3) && (node.textContent.trim().length > 0)) A[A.length]=node;"+
                                "else A = A.concat(deepText(node));"+
                                "node= node.nextSibling;"+
                                "} } return A; } deepText(document.body).map(function(e) { "+
                            "var rect = e.parentElement.getClientRects()[0]; return [ e.textContent.trim(), rect.top, rect.bottom, rect.left, rect.right ];  }); ";
                            //wholeText,length,data,previousSibling,parentNode,lastChild,baseURI,firstChild,nodeValue,textContent,nodeType,nodeName,prefix,childNodes,nextSibling,ownerDocument,namespaceURI,localName,parentElement
                            
                            
                            JSObject result = (JSObject) engine.executeScript(detect);
                            int length = (int)result.getMember("length");
                            for (int i = 0; i < length; i++)
                                System.out.println(i + " "+ result.getSlot(i));
                                    
                    
                    }
                }
            });
          
          
        Thread.sleep(100);
        
        Document d = engine.getDocument();
        for (Node n : getAllNodes(view)) {
            System.out.println(n);
        }
        
    }
 
    
    public static ArrayList<Node> getAllNodes(Parent root) {
        ArrayList<Node> nodes = new ArrayList<Node>();
        addAllDescendents(root, nodes);
        return nodes;
    }

    private static void addAllDescendents(Parent parent, ArrayList<Node> nodes) {
        for (Node node : parent.getChildrenUnmodifiable()) {
            nodes.add(node);
            if (node instanceof Parent)
                addAllDescendents((Parent)node, nodes);
        }
    }

    public static void main(String[] args) throws IOException {
        Application.launch(args);
    }
}