/*****************************************************************************
 *
 * Copyright (c) 2008-17, Joachim Fellmuth, Holger Gross, Florian Greiner, 
 * Bettina Hünnemeyer, Paula Herber, Verena Klös, Timm Liebrenz, 
 * Tobias Pfeffer, Marcel Pockrandt, Rolf Schröder, Simon Schwan
 * Technische Universitaet Berlin, Software and Embedded Systems
 * Engineering Group, Ernst-Reuter-Platz 7, 10587 Berlin, Germany.
 * All rights reserved.
 * 
 * This file is part of STATE (SystemC to Timed Automata Transformation Engine).
 * 
 * STATE is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * STATE is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with STATE.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 *  Please report any problems or bugs to: state@pes.tu-berlin.de
 *
 ****************************************************************************/

package de.tub.pes.syscir.engine.typetransformer;

import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.tub.pes.syscir.engine.Environment;
import de.tub.pes.syscir.engine.TransformerFactory;
import de.tub.pes.syscir.engine.util.Constants;
import de.tub.pes.syscir.engine.util.Pair;
import de.tub.pes.syscir.sc_model.SCClass;
import de.tub.pes.syscir.sc_model.SCVariable;
import de.tub.pes.syscir.sc_model.expressions.ConstantExpression;
import de.tub.pes.syscir.sc_model.expressions.Expression;
import de.tub.pes.syscir.sc_model.expressions.SCVariableDeclarationExpression;
import de.tub.pes.syscir.sc_model.variables.SCClassInstance;
import de.tub.pes.syscir.sc_model.variables.SCKnownType;

/**
 * This class represents a fifo_type channel.
 * 
 * @author Pfeffer
 */
public class SCFifoTypeTransformer extends AbstractTypeTransformer {

	private static Logger logger = LogManager.getLogger(SCFifoTypeTransformer.class
			.getName());
	
	/**
	 * creates the SCClasses for sc_fifo dependending on data type & fifo size
	 * classes will be named: sc_fifo_TYPE / sc_fifo_TYPE_SIZE
	 * therefore the method reads the sc_fifo*.ast.xml implementations
	 * and replaces GTYPE and GSIZE
	 */
	@Override
	public void createType(Environment e) {
		//sc_fifo declaration without instantiation
		//build generic template without fifo size: sc_fifo_TYPE
		if (!e.getLastType().isEmpty() && !e.getLastType().peek().equals("sc_fifo")) {
			
			String fifoType = e.getLastType().peek();
			this.name = createName(fifoType);
			if (!existsType(name, e)) {
				Environment temp = createEnvironment(e, fifoType);
				temp.getCurrentClass().setName(this.name);
				e.integrate(temp);
			}
		}
		//fifo instantiation
		//build generic type with size: sc_fifo_TYPE_SIZE
		//default size = 16
		else if (
			!e.getLastType().isEmpty() && e.getLastType().peek().equals("sc_fifo") &&
			!e.getLastArgumentList().isEmpty())
		{
			String size = ((ConstantExpression) e.getLastArgumentList().get(0)).getValue();
	
			
			String type = e.getLastType_TemplateArguments().get(0);			
			//Build template and replace BUF_SIZE
			this.name = createName(type, size);
			if (!existsType(this.name, e)) {
				Environment temp = createEnvironment(e, type);
				temp.getCurrentClass().setName(name);
				addSize(size, temp.getCurrentClass());
				e.integrate(temp);
			}	
		}
		//Size not known, type should already be created
		else {
			int x = 0;
		}
	}
	
	/**
	 * returns a KnownType for the instantiated sc_fifo_*
	 */
	@Override
	public SCKnownType initiateInstance(String instName,
			List<Expression> params, Environment e, boolean stat, boolean cons,
			List<String> other_mods) {	
		
		// create (now) known type
		SCKnownType kt = super.initiateInstance(instName,
				new LinkedList<Expression>(), e, stat, cons, other_mods);

		return kt;
	}

	/**
	 * creates an Environment-Object of the sc_fifo implementation
	 * therefore it uses
	 * - sc_fifo_complex.ast.xml for data type with generic parameters (i.e. sc_uint<XX>)
	 * - sc_fifo.ast.xml for simple data typen (i.e. bool, int, ...)
	 * 
	 * @param e
	 * @param type
	 * @return
	 */
	private Environment createEnvironment(Environment e, String type) {
		
		Pair<String, String>[] replacements;
		
		if (type.contains("<")) {
			replacements = new Pair[3];
			this.impl = TransformerFactory.getImplementation("sc_fifo", "sc_fifo_generic.ast.xml");
			String typeIdentifier = type.substring(0, type.indexOf("<"));
			String length = type.substring(type.indexOf("<")+1, type.length()-1);
			replacements[0] = new Pair<String, String>(Constants.GENERIC_TYPE, typeIdentifier);
			replacements[1] = new Pair<String, String>(Constants.GENERIC_TYPE_LENGTH, length);
			replacements[2] = new Pair<String, String>("sc_fifox", "sc_fifo_" + typeIdentifier);
		} else {
			replacements = new Pair[2];
			replacements[0] = new Pair<String, String>(Constants.GENERIC_TYPE, type);
			replacements[1] = new Pair<String, String>("sc_fifox", "sc_fifo_" + type);
		}
		
		return super.createGenericType(e, replacements);
	}
	
	/**
	 * eliminates special characters from the data type so that valid names for
	 * the corresponding SCClass are created
	 * @param type
	 * @return
	 */
 	private static String typeForTemplate(String type) {
		return type
				.replace(" ", "")
				.replace("<", "")
				.replace(">", "")
				.replace("_", "");
	}
 	
 	/**
 	 * Changes the size of the sc_fifo to the given one
 	 * DOES NOT CHANGE THE NAME!!!
 	 * 
 	 * @param size
 	 * @param scClass
 	 */
	public static void addSize(String size, SCClass scClass) {
		SCVariable bufferSizeVar = scClass.getMemberByName("BUF_SIZE");
		if (bufferSizeVar != null) {
			SCVariableDeclarationExpression declarationExpression = bufferSizeVar.getDeclaration();
			ConstantExpression initialValue = (ConstantExpression) declarationExpression.getInitialValues().get(0);
			initialValue.setValue(size+"");
		} else {
			logger.warn("It seems that sc_fifo implementation was changed but SCFifoTypeTransformer was not updated! Buffer size could not be changed");
		}
	}
 	
	/**
	 * creates the name of the class: sc_fifo_TYPE
	 * @param type
	 * @return
	 */
 	public static String createName(String type) {
 		return "sc_fifo" + Constants.GENERIC_TYPE_DELIMITER + typeForTemplate(type);
 	}
 	
 	/**
 	 * creates the name of the class: sc_fifo_TYPE_SIZE
 	 * @param type
 	 * @param size
 	 * @return
 	 */
 	public static String createName(String type, String size) {
 		return "sc_fifo" +
 				Constants.GENERIC_TYPE_DELIMITER + typeForTemplate(type) +
 				Constants.GENERIC_TYPE_DELIMITER + size;
 	}
 	
 	/**
 	 * appends the size to the given fifoName
 	 * 
 	 * @param fifoName should include the type
 	 * @param size
 	 * @return fifoName_SIZE
 	 */
 	public static String appendSize(String fifoName, String size) {
 		return fifoName + Constants.GENERIC_TYPE_DELIMITER + size;
 	}
 	
}
