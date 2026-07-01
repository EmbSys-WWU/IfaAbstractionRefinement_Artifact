package de.tub.pes.syscir.engine.nodetransformer;

import org.w3c.dom.Node;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.util.NodeUtil;
import de.tub.pes.syscir.sc_model.SCFunction;
import de.tub.pes.syscir.sc_model.expressions.GoalAnnotation;

public class CommentTransformer extends AbstractNodeTransformer {
	public void transformNode(Node node, Environment e) {
		if (node != null) {
			String name = NodeUtil.getAttributeValueByName(node, "name");
			if (name.equals("// GOAL")) {
				GoalAnnotation gA = new GoalAnnotation(node);
				e.getExpressionStack().add(gA);
			}
		}
	}
}
