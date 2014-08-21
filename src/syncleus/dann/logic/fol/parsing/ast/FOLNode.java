package syncleus.dann.logic.fol.parsing.ast;

import java.util.List;

import syncleus.dann.logic.io.aima.ParseTreeNode;
import syncleus.dann.logic.fol.parsing.FOLVisitor;

/**
 * @author Ravi Mohan
 * @author Ciaran O'Reilly
 */
public interface FOLNode extends ParseTreeNode {
	String getSymbolicName();

	boolean isCompound();

	List<? extends FOLNode> getArgs();

	Object accept(FOLVisitor v, Object arg);

	FOLNode copy();
}
