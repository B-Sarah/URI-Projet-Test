package org.eclipse.emf.common.util;

import org.eclipse.emf.common.util.Pool.AccessUnit;

public abstract class Queue extends AccessUnit.Queue<URI>{
	/**
	 * 
	 */
	private static final long serialVersionUID = 9159980699689433680L;
	protected abstract AccessUnit<URI> newAccessUnit();
}
