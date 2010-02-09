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

import java.util.List;

import org.alfresco.service.namespace.QName;

/**
 * Implementazione dell'algoritmo quick-sort.
 * 
 * @author Doqui
 */
@SuppressWarnings("unchecked")
public final class QuickSort extends Sort
{

   public QuickSort(List data, QName field, boolean bForward, String mode, NodeSvc nodeService)
   {
      super(data, field, bForward, mode, nodeService);
   }
     
   public void sort()
   {
      if (this.data.size() != 0)
      {
         qsort(this.data, 0, this.data.size() - 1);
      }
   }
   
   private void qsort(final List v, final int lower, final int upper)
   {
      int sliceLength = upper - lower + 1 ;
      if (sliceLength > 1)
      {
         if (sliceLength < 7)
         {
            // Insertion sort on smallest datasets
            for (int i=lower; i<=upper; i++)
            {
               if (this.bForward == true)
               {
                  for (int j=i; j > lower && getComparator().compare(this.keys.get(j - 1), this.keys.get(j)) > 0; j--)
                  {
                     // swap both the keys and the actual row data
                     swap(this.keys, j - 1, j);
                     swap(v, j - 1, j);
                  }
               }
               else
               {
                  for (int j=i; j > lower && getComparator().compare(this.keys.get(j - 1), this.keys.get(j)) < 0; j--)
                  {
                     // swap both the keys and the actual row data
                     swap(this.keys, j - 1, j);
                     swap(v, j - 1, j);
                  }
               }
            }
         }
         else
         {
            int pivotIndex = partition(v, lower, upper);
            qsort(v, lower, pivotIndex);
            qsort(v, pivotIndex + 1, upper);
         }
      }
   }

   private int partition(final List v, int lower, int upper)
   {
      List keys = this.keys;
      Object pivotValue = keys.get((upper + lower + 1) >> 1) ;
      
      while (lower <= upper)
      {
         if (this.bForward == true)
         {
            while (getComparator().compare(keys.get(lower), pivotValue) < 0)
            {
               lower++;
            }
            while (getComparator().compare(pivotValue, keys.get(upper)) < 0)
            {
               upper--;
            }
         }
         else
         {
            while (getComparator().compare(keys.get(lower), pivotValue) > 0)
            {
               lower++;
            }
            while (getComparator().compare(pivotValue, keys.get(upper)) > 0)
            {
               upper--;
            }
         }
         if (lower <= upper)
         {
            if (lower < upper)
            {
               swap(keys, lower, upper);
               swap(v, lower, upper);
            }
            lower++;
            upper--;
         }
      }
      
      return upper;
   }
   
}
