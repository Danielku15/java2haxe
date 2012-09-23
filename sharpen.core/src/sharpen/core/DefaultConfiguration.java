/* Copyright (C) 2004 - 2010  Versant Inc.  http://www.db4o.com

This file is part of the sharpen open source java to c# translator.

sharpen is free software; you can redistribute it and/or modify it under
the terms of version 2 of the GNU General Public License as published
by the Free Software Foundation and as clarified by db4objects' GPL 
interpretation policy, available at
http://www.db4o.com/about/company/legalpolicies/gplinterpretation/
Alternatively you can write to db4objects, Inc., 1900 S Norfolk Street,
Suite 350, San Mateo, CA 94403, USA.

sharpen is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. */
package sharpen.core;

public class DefaultConfiguration extends Configuration {

	DefaultConfiguration(String runtimeTypeName) {
		super(runtimeTypeName);
		
		setUpPrimitiveMappings();
		setUpAnnotationMappings();
	
		mapType("java.lang.System", runtimeTypeName);
		mapType("java.lang.Math", "haxen.Math");
		
		setUpIoMappings();
	
		setUpExceptionMappings();
	    
	    setUpCollectionMappings();
	    
	    mapType("java.lang.Cloneable", "haxen.ICloneable");
	    
	    mapType("java.util.Date", "haxen.util.Date");
	
	    mapMethod("java.lang.Object.hashCode", "hashCode");
	    mapMethod("java.lang.Object.equals", "equals");
	    
	    mapMethod("java.lang.Float.isNaN", "Math.isNaN");
	    mapMethod("java.lang.Double.isNaN", "Math.isNaN");
	    
	    setUpStringMappings();
	
	    mapMethod("java.lang.Throwable.printStackTrace", runtimeMethod("printStackTrace"));
	    
	    mapMethod("java.lang.System.arraycopy", runtimeMethod("arrayCopy"));
	    mapMethod("java.lang.Object.wait", runtimeMethod("wait"));
	    mapMethod("java.lang.Object.notify", runtimeMethod("notify"));
	    mapMethod("java.lang.Object.notifyAll", runtimeMethod("notifyAll"));
	    mapMethod("java.lang.Object.getClass", runtimeMethod("getClassForObject"));		
	
		setUpPrimitiveWrappers();		
	}
	
	private void setUpPrimitiveMappings() {
		mapType("boolean", "Bool");
		mapType("void", "Void");
		mapType("char", "Char");
		mapType("byte", "Byte");
		mapType("short", "Short");
		mapType("int", "Int");
		mapType("long", "Long");
		mapType("float", "Float");
		mapType("double", "Double");
		
		mapType("java.lang.Object", "haxen.Object");
		mapType("java.lang.String", "String");
		mapType("java.lang.Character", "haxen.Char");
		mapType("java.lang.Byte", "haxen.Byte");
		mapType("java.lang.Boolean", "haxen.Bool");
		mapType("java.lang.Short", "haxen.Short");
		mapType("java.lang.Integer", "haxen.Int");
		mapType("java.lang.Long", "haxen.Long");
		mapType("java.lang.Float", "haxen.Float");
		mapType("java.lang.Double", "haxen.Double");
	}
	
	private void setUpCollectionMappings() {
		// collection framework
	    mapType("java.util.Collection", "haxen.collections.ICollection");
	    mapType("java.util.Collection<>", "haxen.collections.generic.ICollection");
	    mapType("java.util.Set<>", "haxen.collections.generic.ICollection");
	    if (mapIteratorToEnumerator()) {
	    	mapType("java.util.Iterator", "haxen.collections.IEnumerator");
	    	mapType("java.util.Iterator<>", "haxen.collections.generic.IEnumerator");
	    	mapType("java.lang.Iterable", "haxen.collections.IEnumerable");
	    	mapType("java.lang.Iterable<>", "haxen.collections.generic.IEnumerable");
	    }
	    mapType("java.util.Map", "haxen.collections.IMap");
	    mapType("java.util.Map<,>", "haxen.collections.generic.IMap");
	    mapType("java.util.Map.Entry", "haxen.collections.MapEntry");
	    mapType("java.util.Map.Entry<,>", "haxen.collections.generic.MapEntry");
	    mapType("java.util.HashMap", "haxen.collections.HashMap");
	    mapType("java.util.HashMap<,>", "haxen.collections.generic.HashMap");
	    mapType("java.util.TreeMap", "haxen.collections.TreeMap");
	    mapType("java.util.TreeMap<,>", "haxen.collections.generic.TreeMap");	    
	    mapType("java.util.SortedMap<,>", "haxen.collections.generic.SortedMap");	    
	    mapType("java.util.List", "haxen.collections.IList");
	    mapType("java.util.List<>", "haxen.collections.generic.IList");
	    mapType("java.util.ArrayList", "haxen.collections.ArrayList");
	    mapType("java.util.ArrayList<>", "haxen.collections.generic.ArrayList");
	    mapType("java.util.LinkedList", "haxen.collections.LinkedList");
	    mapType("java.util.LinkedList<>", "haxen.collections.generic.LinkedList");
	    mapType("java.util.Stack", "haxen.collections.Stack");	    
	    
	    mapType("java.util.Comparator", "haxen.collections.IComparator");
	    
	    if (mapIteratorToEnumerator()) {
	    	mapMethod("java.lang.Iterable.iterator", "getEnumerator");
	    	mapMethod("java.util.Collection.iterator", "getEnumerator");
	    	mapMethod("java.util.List.iterator", "getEnumerator");
	    	mapMethod("java.util.Set.iterator", "getEnumerator");
	    }
	    
	    //  jdk 1.0 collection framework
	    mapType("java.util.Vector", "haxen.collections.ArrayList");
	    mapType("java.util.Enumeration", "haxen.collections.IEnumerator");
	    mapProperty("java.util.Vector.size", "Count");
	    // converter thinks size belong to AbstractCollection on jdk 6
	    mapMethod("java.util.Vector.elements", "getEnumerator");
	    mapMethod("java.util.Vector.copyInto", "copyTo");
	    mapMethod("java.util.Vector.removeAllElements", "clear");
	    
	    mapType("java.util.Hashtable", "haxen.collections.Hashtable");
	    
		/*// JUnit
		mapNamespace("junit.framework", "NUnit.Framework");
		mapMethod("junit.framework.Assert.assertEquals", "NUnit.Framework.Assert.AreEqual");
		mapMethod("junit.framework.Assert.assertTrue", "NUnit.Framework.Assert.IsTrue");
		mapMethod("junit.framework.Assert.assertFalse", "NUnit.Framework.Assert.IsFalse");
		mapMethod("junit.framework.Assert.assertNotNull", "NUnit.Framework.Assert.IsNotNull");
		mapMethod("junit.framework.Assert.assertNull", "NUnit.Framework.Assert.IsNull");
		mapMethod("junit.framework.Assert.assertSame", "NUnit.Framework.Assert.AreSame");
		mapMethod("junit.framework.Assert.assertNotSame", "NUnit.Framework.Assert.AreNotSame");
	    
		// JUnit 4
		mapNamespace("org.junit", "NUnit.Framework");
		mapMethod("org.junit.Assert.assertEquals", "NUnit.Framework.Assert.AreEqual");
		mapMethod("org.junit.Assert.assertTrue", "NUnit.Framework.Assert.IsTrue");
		mapMethod("org.junit.Assert.assertFalse", "NUnit.Framework.Assert.IsFalse");
		mapMethod("org.junit.Assert.assertNotNull", "NUnit.Framework.Assert.IsNotNull");
		mapMethod("org.junit.Assert.assertNull", "NUnit.Framework.Assert.IsNull");
		mapMethod("org.junit.Assert.assertSame", "NUnit.Framework.Assert.AreSame");
		mapMethod("org.junit.Assert.assertNotSame", "NUnit.Framework.Assert.AreNotSame");
		mapMethod("org.junit.Assert.fail", "NUnit.Framework.Assert.Fail");
		mapType("org.junit.Assert", "NUnit.Framework.Assert");
		mapType("org.junit.Before", "NUnit.Framework.SetUp");
		mapType("org.junit.After", "NUnit.Framework.TearDown");*/
	}
	
	private void setUpExceptionMappings() {
		mapType("java.lang.Throwable", "haxen.Exception");
		mapType("java.lang.Error", "haxen.Exception");
		mapType("java.lang.Exception", "haxen.Exception");
		mapType("java.lang.RuntimeException", "haxen.Exception");
		mapType("java.lang.ClassCastException", "haxen.ClassCastException");
		mapType("java.lang.NullPointerException", "haxen.NullPointerException");
		mapType("java.lang.IllegalArgumentException", "haxen.ArgumentException");
		mapType("java.lang.IllegalStateException", "haxen.InvalidOperationException");
		mapType("java.lang.InterruptedException", "haxen.Exception");
	    mapType("java.lang.IndexOutOfBoundsException", "haxen.IndexOutOfRangeException");
	    mapType("java.lang.UnsupportedOperationException", "haxen.NotSupportedException");
	    mapType("java.lang.ArrayIndexOutOfBoundsException", "haxen.IndexOutOfRangeException");
	    mapType("java.lang.NoSuchMethodError", "haxen.MissingMethodException");
	    mapType("java.io.IOException", "haxen.io.IOException");
	}
	
	private void setUpPrimitiveWrappers() {
	    mapField("java.lang.Short.MAX_VALUE", "haxen.Short.MAX_VALUE");
		mapField("java.lang.Short.MIN_VALUE", "haxen.Short.MIN_VALUE");
		mapField("java.lang.Integer.MAX_VALUE", "haxen.Integer.MAX_VALUE");
		mapField("java.lang.Integer.MIN_VALUE", "haxen.Integer.MIN_VALUE");
		mapField("java.lang.Long.MAX_VALUE", "haxen.Long.MAX_VALUE");
		mapField("java.lang.Long.MIN_VALUE", "haxen.Long.MIN_VALUE");
		mapField("java.lang.Float.MAX_VALUE", "haxen.Float.MAX_VALUE");
		mapField("java.lang.Float.MIN_VALUE", "haxen.Float.MIN_VALUE");
		mapField("java.lang.Float.POSITIVE_INFINITY", "haxen.Float.POSITIVE_INFINITY");
		mapField("java.lang.Float.NEGATIVE_INFINITY", "haxen.Float.NEGATIVE_INFINITY");
		mapField("java.lang.Double.MAX_VALUE", "haxen.Double.MAX_VALUE");
		mapField("java.lang.Double.MIN_VALUE", "haxen.Double.MIN_VALUE");
		mapField("java.lang.Double.NEGATIVE_INFINITY", "haxen.Double.NEGATIVE_INFINITY");
		mapField("java.lang.Double.POSITIVE_INFINITY", "haxen.Double.POSITIVE_INFINITY");
		mapField("java.lang.Boolean.TRUE", "true");
		mapField("java.lang.Boolean.FALSE", "false");
		mapField("java.lang.Byte.MAX_VALUE", "haxen.Byte.MAX_VALUE");
		mapField("java.lang.Byte.MIN_VALUE", "haxen.Byte.MIN_VALUE");
		mapField("java.lang.Character.MAX_VALUE", "haxen.Character.MAX_VALUE");
		mapField("java.lang.Character.MIN_VALUE", "haxen.Character.MIN_VALUE");
		
		mapWrapperConstructor("java.lang.Boolean.Boolean", "haxen.Convert.toBoolean", "Bool");
		mapWrapperConstructor("java.lang.Byte.Byte", "haxen.Convert.toByte", "Byte");
		mapWrapperConstructor("java.lang.Character.Character", "haxen.Convert.toChar", "Char");
		mapWrapperConstructor("java.lang.Short.Short", "haxen.Convert.toInt16", "Short");
		mapWrapperConstructor("java.lang.Integer.Integer", "haxen.Convert.toInt32", "Int");
		mapWrapperConstructor("java.lang.Long.Long", "haxen.Convert.toInt64", "Long");
		mapWrapperConstructor("java.lang.Float.Float", "haxen.Convert.toSingle", "Float");
		mapWrapperConstructor("java.lang.Double.Double", "haxen.Convert.toDouble", "Float");
    }	
	
	public boolean isIgnoredExceptionType(String exceptionType) {
		return exceptionType.equals("java.lang.CloneNotSupportedException");
	}	

	@Override
	public boolean mapByteToSbyte() {
		return false;
	}
}

