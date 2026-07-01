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

package de.tub.pes.syscir.engine.util;

/**
 * Container class that holds two values of arbitrary types. Useful if you want
 * to return two values at once without using Arrays or Containers with
 * arbitrary size.
 * 
 * @author pockrandt
 * 
 * @param <E1>
 * @param <E2>
 */
public class Pair<E1, E2> {

	private E1 first;
	private E2 second;

	public Pair(E1 first, E2 second) {
		this.first = first;
		this.second = second;
	}

	/**
	 * Returns the first element of the pair.
	 * @return
	 */
	public E1 getFirst() {
		return first;
	}

	/**
	 * Returns the second element of the pair.
	 * @return
	 */
	public E2 getSecond() {
		return second;
	}

	@Override
	public int hashCode() {
		return first.hashCode() + second.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Pair<?, ?>) {
			Pair<?, ?> op = (Pair<?, ?>) o;
			return this.first.equals(op.first) && this.second.equals(op.second);
		} else {
			return false;
		}
	}

	@Override
	public String toString() {
		return first.toString() + " " + second.toString();
	}

	public boolean swap() {
		// this wont work if fst/snd super/sub classes
		if (first.getClass().equals(second.getClass())) {
			E1 tmp = first;
			first = (E1) second;
			second = (E2) tmp;
			return true;
		} else {
			return false;
		}
	}
}
