package edu.msu.frib.daolog.utils;

import java.util.Iterator;

import edu.msu.frib.daolog.exception.EmptySetException;
import edu.msu.frib.daolog.exception.TooManyElementsException;

public class DataUtils {

	
	public static <E> E getOnlyElement(Iterable<E> iterable) throws EmptySetException, TooManyElementsException {
		Iterator<E> iterator = iterable.iterator();
		
		if (!iterator.hasNext()) {
		    throw new EmptySetException("Collection is empty");
		}

		E element = iterator.next();

		if (iterator.hasNext()) {
		    throw new TooManyElementsException("Collection contains more than one item");
		}

		return element;
	}
}
