/*
 * Copyright 2012 Amazon Technologies, Inc.
 * 
 * Licensed under the Amazon Software License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 * 
 * http://aws.amazon.com/asl
 * 
 * This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES
 * OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and
 * limitations under the License.
 */ 


package com.amazonaws.mturk.util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


public class WsdlEnumUtil {

    /**
     * Retrieves an array of the possible types for a WSDL enum.
     * 
     * @param <T>
     * @param wsdlEnumClass
     * @return
     */
    public static <T> T[] getValues(Class<T> wsdlEnumClass) {
        List<T> values = new ArrayList<T>();
        for (Field field : wsdlEnumClass.getFields()) {
            if (Modifier.isPublic(field.getModifiers()) &&
                Modifier.isFinal(field.getModifiers()) &&
                Modifier.isStatic(field.getModifiers()) &&
                field.getType().equals(wsdlEnumClass))
            {
                try {
                    T value = (T) field.get(null);
                    if (value != null) {
                        values.add(value);
                    }
                }
                catch (IllegalAccessException iae) {
                }
            }
        }
        return values.toArray((T[]) Array.newInstance(wsdlEnumClass, values.size()));
    }
    
    /**
     * Retrieves the WSDL enum value from wsdlEnumClass that matches, case-insensitive.
     * 
     * @param <T>
     * @param wsdlEnumClass
     * @param enumString
     * @return
     * @throws IllegalArgumentException
     */
    public static <T> T fromStringIgnoreCase(Class<T> wsdlEnumClass, String enumString)
        throws IllegalArgumentException
    {
        for (T value : getValues(wsdlEnumClass)) {
            if (value.toString().equalsIgnoreCase(enumString)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Invalid value [" + enumString + "] for type " + wsdlEnumClass + ".");
    }
    
    /**
     * Returns the possible values for a WSDL enum class in the form:
     *   A, B or C
     * @return
     */
    public static <T> String getValuesPossibilityDescription(Class<T> wsdlEnumClass) {
        StringBuilder sb = new StringBuilder();
        T[] values = getValues(wsdlEnumClass);
        for (int i=0; i<values.length; i++) {
            if (i == (values.length - 1) && i > 0) {
                sb.append(" or ");
            }
            else if (i > 0) {
                sb.append(", ");
            }
            sb.append(values[i].toString());
        }
        return sb.toString();
    }
}
