/* Copyright (C) 2004 - 2008  Versant Inc.  http://www.db4o.com

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

package sharpen.core.csharp.ast;

import java.util.*;

public abstract class CSTypeDeclaration extends CSType implements CSTypeParameterProvider {
	
	protected List<CSMember> _members = new ArrayList<CSMember>();
	
	protected CSTypeReferenceExpression _baseType;
	protected List<CSTypeReferenceExpression> _interfaces = new ArrayList<CSTypeReferenceExpression>();
	
	protected List<CSTypeParameter> _typeParameters = new ArrayList<CSTypeParameter>();

	private boolean _partial;
	private int _constructorCount;

	CSTypeDeclaration(String name) {
		super(name);
	}
	
	public boolean isSealed() {
		return false;
	}
	
	public boolean isInterface() {
		return false;
	}

	public void addMember(CSMember member) {
		_members.add(member);
	}
	
	public int constructorCount() {
		return _constructorCount;
	}

	public List<CSMember> members() {
		return Collections.unmodifiableList(_members);
	}
	
	public List<CSConstructor> constructors() {
		ArrayList<CSConstructor> ctors = new ArrayList<CSConstructor>();
		for (CSMember member : _members) {
			if (member instanceof CSConstructor) {
				ctors.add((CSConstructor) member);
			}
		}
		return Collections.unmodifiableList(ctors);
	}

	public void addInterface(CSTypeReferenceExpression typeRef) {
		_interfaces.add(typeRef);
	}
	
	public void clearInterfaces() {
		_interfaces.clear();
	}
	
	public List<CSTypeReferenceExpression> interfaces() {
		return Collections.unmodifiableList(_interfaces);
	}
	
	public CSTypeReferenceExpression baseType() {
		return _baseType;
	}
	
	public void baseType(CSTypeReferenceExpression baseType) {
		_baseType = baseType;
	}

	public void addTypeParameter(CSTypeParameter typeParameter) {
		_typeParameters.add(typeParameter);
	}
	
	public List<CSTypeParameter> typeParameters() {
		return Collections.unmodifiableList(_typeParameters);
	}

	public void partial(boolean partial) {
		_partial = partial;
	}
	
	public boolean partial() {
		return _partial;
	}

	public CSMember getMember(String name) {
		for (CSMember member : _members) {
			if (member.name().equals(name)) {
				return member;
			}
		}
		return null;
	}
}
