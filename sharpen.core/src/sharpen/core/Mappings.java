/* Copyright (C) 2009  Versant Inc.   http://www.db4o.com */
package sharpen.core;

import org.eclipse.jdt.core.dom.*;

import sharpen.core.Configuration.*;
import sharpen.core.csharp.ast.CSExpression;

public interface Mappings {

	String mappedFieldName(IVariableBinding binding);

	String mappedTypeName(ITypeBinding type);

	String mappedMethodName(IMethodBinding binding);

	MemberMapping effectiveMappingFor(IMethodBinding binding);

	String currentNamespace();
	
	void currentNamespace(String currentNamespace);

	String constructorMethod(ITypeBinding type, IMethodBinding ctor);
}
