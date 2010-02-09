/* Index ECM Engine - A system for managing the capture (when created 
 * or received), classification (cataloguing), storage, retrieval, 
 * revision, sharing, reuse and disposition of documents.
 *
 * Copyright (C) 2008 Regione Piemonte
 * Copyright (C) 2008 Provincia di Torino
 * Copyright (C) 2008 Comune di Torino
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */
 
package it.doqui.index.ecmengine.business.publishing.util;

import it.doqui.index.ecmengine.business.foundation.repository.NodeSvc;

import java.sql.Timestamp;
import java.text.CollationKey;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.namespace.QName;
import org.apache.log4j.Logger;

/**
 * Classe astratta per l'ordinamento di elementi.
 * 
 * @author Doqui
 */
@SuppressWarnings("unchecked")
public abstract class Sort {

	public final static String SORT_CASEINSENSITIVE = "case-insensitive";
	public final static String SORT_CASESENSITIVE = "case-sensitive";

	/** list of Object[] data to sort */
	protected List data;

	/** column name to sort against */
	protected QName field;

	/** sort direction */
	protected boolean bForward;

	/** sort mode (see IDataContainer constants) */
	protected String sortMode;

	/** locale sensitive collator */
	protected Collator collator;

	/** collation keys for comparisons */
	protected List keys = null;

	/** the comparator instance to use for comparing values when sorting */
	private Comparator comparator = null;

	// TODO: make this configurable
	/** config value whether to use strong collation Key string comparisons */
	private boolean strongStringCompare = false;

	protected NodeSvc nodeService;

   protected static Logger s_logger = Logger.getLogger(Sort.class);

	/**
	 * Costruttore predefinito.
	 * 
	 * @param data L'oggetto {@code List} da ordinare. 
	 * @param field {@code QName} della property su cui eseguire l'ordinamento.
	 * @param bForward true for a forward sort, false for a reverse sort
	 * @param mode Sort mode da utilizzare (case sensitive/case insensitive).
	 * @param nodeService Il {@code NodeSvc} da utilizzare nelle operazioni.
	 */
	public Sort(List data, QName field, boolean bForward, String mode, NodeSvc nodeService) {
		this.data = data;
		this.field = field;
		this.bForward = bForward;
		this.sortMode = mode;
		this.nodeService = nodeService;

		if (this.data.size() != 0) {
			// setup the Collator for our Locale
			Collator collator = Collator.getInstance(Locale.getDefault());

			// set the strength according to the sort mode
			if (mode.equals(SORT_CASEINSENSITIVE)) {
				collator.setStrength(Collator.SECONDARY);
			} else {
				collator.setStrength(Collator.IDENTICAL);
			}

			this.keys = buildCollationKeys(collator);
		}
	}

	/**
	 * Metodo che avvia l'ordinamento.
	 */
	public abstract void sort();

	/**
	 * Build a list of collation keys for comparing locale sensitive strings or build
	 * the appropriate objects for comparison for other standard data types.
	 * 
	 * @param collator      the Collator object to use to build String keys
	 */
	@SuppressWarnings("unchecked")
	protected List buildCollationKeys(Collator collator) {
		List data = this.data;
		int iSize = data.size();
		List keys = new ArrayList(iSize);

		try {
			// there will always be at least one item to sort if we get to this method
	         Class returnType = null;
			Object bean = this.data.get(0);

			//Stefano LB(ordinare Lista NodeRef per selectNodes)			
			Object obj = null;
			if(bean instanceof NodeRef){
				obj = nodeService.getProperties((NodeRef)bean).get(this.field);
			}else{
				obj = nodeService.getProperties(((ResultSetRow)bean).getNodeRef()).get(this.field);				
			}
			
			if (obj != null) {
				returnType = obj.getClass();
			} else {
				if (s_logger.isInfoEnabled()) {
					s_logger.info("Unable to get return type class for field: " + field);
				}
				returnType = Object.class;
			}

			// create appropriate comparator instance based on data type
			// using the strategy pattern so  sub-classes of Sort simply invoke the
			// compare() method on the comparator interface - no type info required
			boolean bknownType = true;
			if (returnType.equals(String.class)) {
				if (strongStringCompare == true) {
					this.comparator = new StringComparator();
				} else {
					this.comparator = new SimpleStringComparator();
				}
			} else if (returnType.equals(Date.class)) {
				this.comparator = new DateComparator();
			} else if (returnType.equals(boolean.class)
					|| returnType.equals(Boolean.class)) {
				this.comparator = new BooleanComparator();
			} else if (returnType.equals(int.class)
					|| returnType.equals(Integer.class)) {
				this.comparator = new IntegerComparator();
			} else if (returnType.equals(long.class)
					|| returnType.equals(Long.class)) {
				this.comparator = new LongComparator();
			} else if (returnType.equals(float.class)
					|| returnType.equals(Float.class)) {
				this.comparator = new FloatComparator();
			} else if (returnType.equals(double.class)
					|| returnType.equals(Double.class)) {
				this.comparator = new DoubleComparator();
			} else if (returnType.equals(Timestamp.class)) {
				this.comparator = new TimestampComparator();
			} else {
				s_logger.warn("Unsupported sort data type: " + returnType
						+ " defaulting to .toString()");
				this.comparator = new SimpleComparator();
				bknownType = false;
			}

			// create a collation key for each required column item in the dataset
			for (int iIndex = 0; iIndex < iSize; iIndex++) {
				
				//Stefano LB(ordinare Lista NodeRef per selectNodes)
				if(data.get(iIndex) instanceof NodeRef){
					obj = nodeService.getProperties((NodeRef)data.get(iIndex)).get(this.field);					
				}else{
					obj = nodeService.getProperties(((ResultSetRow)data.get(iIndex)).getNodeRef()).get(this.field);					
				}
				
				if (obj instanceof String) {
					String str = (String) obj;
					if (strongStringCompare == true) {
						if (str.indexOf(' ') != -1) {
							// quote white space characters or they will be ignored by the Collator!
							int iLength = str.length();
							StringBuilder s = new StringBuilder(iLength + 4);
							char c;
							for (int i = 0; i < iLength; i++) {
								c = str.charAt(i);
								if (c != ' ') {
									s.append(c);
								} else {
									s.append('\'').append(c).append('\'');
								}
							}
							str = s.toString();
						}
						keys.add(collator.getCollationKey(str));
					} else {
						keys.add(str);
					}
				} else if (bknownType == true) {
					// the appropriate wrapper object will be create by the reflection
					// system to wrap primative types e.g. int and boolean.
					// therefore the correct type will be ready for use by the comparator
					keys.add(obj);
				} else {
					if (obj != null) {
						keys.add(obj.toString());
					} else {
						keys.add(null);
					}
				}
			}
		} catch (Exception err) {
			throw new RuntimeException(err);
		}

		return keys;
	}

	/**
	 * Given the array and two indices, swap the two items in the
	 * array.
	 */
	@SuppressWarnings("unchecked")
	protected void swap(final List v, final int a, final int b) {
		Object temp = v.get(a);
		v.set(a, v.get(b));
		v.set(b, temp);
	}

	/**
	 * Return the comparator to be used during column value comparison
	 * 
	 * @return Comparator for the appropriate column data type
	 */
	protected Comparator getComparator() {
		return this.comparator;
	}

	/**
	 * Return the name of the Bean getter method for the specified getter name
	 * 
	 * @param name of the field to build getter method name for e.g. "value"
	 * 
	 * @return the name of the Bean getter method for the field name e.g. "getValue"
	 */
	protected static String getGetterMethodName(String name) {
		return "get" + name.substring(0, 1).toUpperCase()
				+ name.substring(1, name.length());
	}

	// ------------------------------------------------------------------------------
	// Inner classes for data type comparison

	private static class SimpleComparator implements Comparator {
		/**
		 * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(final Object obj1, final Object obj2) {
			if (obj1 == null && obj2 == null)
				return 0;
			if (obj1 == null)
				return -1;
			if (obj2 == null)
				return 1;
			return (obj1.toString()).compareTo(obj2.toString());
		}
	}

	private static class SimpleStringComparator implements Comparator {
		/**
		 * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(final Object obj1, final Object obj2) {
			if (obj1 == null && obj2 == null)
				return 0;
			if (obj1 == null)
				return -1;
			if (obj2 == null)
				return 1;
			return ((String) obj1).compareToIgnoreCase((String) obj2);
		}
	}

	private static class StringComparator implements Comparator {
		/**
		 * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(final Object obj1, final Object obj2) {
			if (obj1 == null && obj2 == null)
				return 0;
			if (obj1 == null)
				return -1;
			if (obj2 == null)
				return 1;
			return ((CollationKey) obj1).compareTo((CollationKey) obj2);
		}
	}

	private static class IntegerComparator implements Comparator {
		/**
		 * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(final Object obj1, final Object obj2) {
			if (obj1 == null && obj2 == null)
				return 0;
			if (obj1 == null)
				return -1;
			if (obj2 == null)
				return 1;
			return ((Integer) obj1).compareTo((Integer) obj2);
		}
	}

	private static class FloatComparator implements Comparator {
		/**
		 * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(final Object obj1, final Object obj2) {
			if (obj1 == null && obj2 == null)
				return 0;
			if (obj1 == null)
				return -1;
			if (obj2 == null)
				return 1;
			return ((Float) obj1).compareTo((Float) obj2);
		}
	}

	private static class DoubleComparator implements Comparator {
		/**
		 * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(final Object obj1, final Object obj2) {
			if (obj1 == null && obj2 == null)
				return 0;
			if (obj1 == null)
				return -1;
			if (obj2 == null)
				return 1;
			return ((Double) obj1).compareTo((Double) obj2);
		}
	}
	
	private static class LongComparator implements Comparator {
		/**
		 * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(final Object obj1, final Object obj2) {
			if (obj1 == null && obj2 == null)
				return 0;
			if (obj1 == null)
				return -1;
			if (obj2 == null)
				return 1;
			return ((Long) obj1).compareTo((Long) obj2);
		}
	}

	private static class BooleanComparator implements Comparator {
		/**
		 * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(final Object obj1, final Object obj2) {
			if (obj1 == null && obj2 == null)
				return 0;
			if (obj1 == null)
				return -1;
			if (obj2 == null)
				return 1;
			return ((Boolean) obj1).equals((Boolean) obj2) ? -1 : 1;
		}
	}

	private static class DateComparator implements Comparator {
		/**
		 * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(final Object obj1, final Object obj2) {
			if (obj1 == null && obj2 == null)
				return 0;
			if (obj1 == null)
				return -1;
			if (obj2 == null)
				return 1;
			return ((Date) obj1).compareTo((Date) obj2);
		}
	}

	private static class TimestampComparator implements Comparator {
		/**
		 * @see org.alfresco.web.data.IDataComparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(final Object obj1, final Object obj2) {
			if (obj1 == null && obj2 == null)
				return 0;
			if (obj1 == null)
				return -1;
			if (obj2 == null)
				return 1;
			return ((Timestamp) obj1).compareTo((Timestamp) obj2);
		}
	}

} // end class Sort
