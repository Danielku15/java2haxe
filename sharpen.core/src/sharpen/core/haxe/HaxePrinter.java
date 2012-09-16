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

package sharpen.core.haxe;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import sharpen.core.csharp.ast.*;
import sharpen.core.io.*;

public class HaxePrinter extends CSVisitor {

	protected IndentedWriter _writer;
	protected CSTypeDeclaration _currentType;
	private int _lastPrintedCommentIndex;
	private List<CSLineComment> _comments;

	public HaxePrinter() {
	}
	
	public void setWriter(Writer writer) {
		_writer = new IndentedWriter(writer);
	}

	public void print(CSCompilationUnit node) {
		_lastPrintedCommentIndex = 0;
		_comments = node.comments();
		try {
			node.accept(this);
		} finally {
			_currentType = null;
			_comments = null;
		}
	}
	
	private List<CSUsing> printableUsingList(Iterable<CSUsing> usings) {
		List<CSUsing> list = new ArrayList<CSUsing>();
		for (CSUsing using : usings) {
			list.add(using);
		}
		Collections.sort(list, new Comparator<CSUsing>() {
			public int compare(CSUsing a, CSUsing b) {
				boolean ia = a.namespace().startsWith("System");
				boolean ib = b.namespace().startsWith("System");
				
				if (ia && ib) return a.namespace().compareTo(b.namespace());
				else if (ia) return -1;
				else if (ib) return 1;
				else return a.namespace().compareTo(b.namespace()); 
			}
		});
		return list;
	}

	static final Pattern META_VARIABLE_PATTERN = Pattern.compile("\\$(\\w+)");
	
	@Override
	public void visit(CSMacroExpression node) {
		node.macro().accept(this);
	}
	
	@Override
	public void visit(CSMacroTypeReference node) {
		node.macro().accept(this);
    }
	
	@Override
	public void visit(CSMacro node) {
		final String template = node.template();
		final Matcher matcher = META_VARIABLE_PATTERN.matcher(template);
		int last = 0;
		while (matcher.find()) {
			write(template.substring(last, matcher.start()));
			
			Object value = node.resolveVariable(matcher.group(1));
			// value is either a single node or a list of nodes
			if (value instanceof CSNode) {
				((CSNode)value).accept(this);
			} else {
				writeCommaSeparatedList((Iterable<CSNode>) value);
			}
			
			last = matcher.end();
		}
		write(template.substring(last));
	}

	public void visit(CSCompilationUnit node) {
		beginEnclosingIfDefs(node);

		if (null != node.namespace()) {
			writeLine("package " + node.namespace() + ";");
			writeLine();
		}
		
		List<CSUsing> usings = printableUsingList(node.usings());
		for (CSUsing using : usings) {
			using.accept(this);
		}
		
		if (usings.size() > 0) _writer.writeLine();


		writeLineSeparatedList(node.types());
		endEnclosingIfDefs(node);
	}
	
	@Override
	public void visit(CSRemovedExpression node) {
	    throw new IllegalStateException("Unexpected removal of expression: " + node.toString());
	}

	public void visit(CSUsing node) {
		writeLine("import " + node.namespace() + ";");
	}
	
	public void visit(CSClass node) {
		writeType(node);
	}
	
	public void visit(CSEnum node) {
		writeMemberHeader(node);
		writeLine("enum " + node.name());
		enterBody();
		writeSeparatedList(node.values(), new Closure() {
			public void execute() {
				writeLine(";");
			}
		});
		writeLine();
		leaveBody();
	}
	
	@Override
	public void visit(CSEnumValue node) {
		writeIndented(node.name());
	}
	
	public void visit(CSStruct node) {
		writeType(node);
	}
	
	public void visit(CSInterface node) {
		writeType(node);
	}

	public void visit(CSTypeParameter node) {
		write(node.name());
		if(node.superClass() != null) {
			write(" : (");
			node.superClass().accept(this);
			write(")");
		}
	}
	
	@Override
	public void visit(final CSArrayTypeReference node) {
	    for (int i = 0; i < node.dimensions(); ++i) {
	        write("Array<");
	    }
	    node.elementType().accept(this);
	    for (int i = 0; i < node.dimensions(); ++i) {
	        write(">");
	    }
	}
	
	public void visit(CSTypeReference node) {
		write(node.typeName());
		writeTypeArguments(node);
	}

	private void writeTypeArguments(CSTypeArgumentProvider node) {
		final List<CSTypeReferenceExpression> typeArgs = node.typeArguments();
		if (!typeArgs.isEmpty()) {
			writeGenericParameters(typeArgs);
		}
	}
	
	public void visit(CSDelegate node) {
	    writeMemberHeader(node);
	    write("typedef ");
	    write(node.name());
	    write(" = ");
	    for (CSVariableDeclaration parameter : node.parameters()) {
	        parameter.type().accept(this);
	        write("->");
	    }
	    write("Void");
	    writeLine(";");		
	}

	private void writeTypeHeader(CSTypeDeclaration node) {
		writeMemberHeader(node);		
		if (node.isInterface()) {
			if (node.partial()) _writer.write("/*partial*/ ");
			write("interface " + node.name());
		} else if (node instanceof CSClass) {
			CSClass classNode = (CSClass)node;
			write(classModifier(classNode.modifier()));
			if (node.partial()) _writer.write("/*partial*/ ");
			write("class " + node.name());
		} else {
			write("/*struct*/ " + node.name());
		}
		writeTypeParameters(node);
		writeBaseTypes(node);
	}

	private void writeMemberHeader(CSTypeDeclaration node) {
		writeAttributes(node);
		// writeVisibility(node);
	}

	private void writeTypeParameters(CSTypeParameterProvider node) {
		final List<CSTypeParameter> parameters = node.typeParameters();
		if (parameters.isEmpty()) return;
		writeGenericParameters(parameters);
	}

	private <T extends CSNode> void writeGenericParameters(Iterable<T> nodes) {
		write("<");
		writeCommaSeparatedList(nodes);
		write(">");
	}

	private void writeType(CSTypeDeclaration node) {
		writeDoc(node);
		beginEnclosingIfDefs(node);
		writeTypeHeader(node);
		writeTypeBody(node);
		endEnclosingIfDefs(node);
	}
	
	private void writeBaseTypes(CSTypeDeclaration node) {
	    List<CSTypeReferenceExpression> baseTypes = node.baseTypes();
	    if (baseTypes.isEmpty()) {
	        return;
	    }

	    String keyword = node.isInterface() ? " implements" : " extends";
	    for (int i = 0; i < baseTypes.size(); i++) {
	        CSTypeReferenceExpression baseType = baseTypes.get(i);
	        if (i > 0) {
	            write(",");
	        }

	        write(keyword);
	        write(" ");
	        baseType.accept(this);
	        if (i == 0) {
	            keyword = "implements";
	        }
	    }
	}

	private void writeTypeBody(CSTypeDeclaration node) {
		writeLine();
		enterBody();
		CSTypeDeclaration saved = _currentType;
		_currentType = node;
		writeLineSeparatedList(node.members());
		_currentType = saved;
		printPrecedingComments(node.startPosition() + node.sourceLength());
		leaveBody();
	}

	private void writeVisibility(CSMember member) {
		writeIndentation();
		
		if (member.isNewModifier())
			write("/*new*/ ");
		
		if (isExplicitMember(member)) return;
		
		CSVisibility visibility = member.visibility();
		switch (visibility) {
			case Internal:
				write("public");
				break;
			case Private:
				write("private");
				break;
			case Protected:
				write("private");
				break;
			case Public:
				write("public");
				break;
		}
		write(" ");
	}

	private boolean isExplicitMember(CSMember member) {
		return member.name().indexOf('.') != -1;
	}
	
	public void visit(CSVariableDeclaration node) {
		if(!node.parameter()) {
			write("var "); 
		}
		if (null != node.name()) {
		    write(node.name());
		}
		write(" : ");
		node.type().accept(this);
		if (null != node.initializer()) {
		    write(" = ");
		    node.initializer().accept(this);
		}
	}
	
	public void visit(CSConstructor node) {
	    writeDoc(node);
	    writeAttributes(node);
	    if (node.isStatic()) {
	        writeIndented("static var __init = (function()");
	        writeLine();
	        node.body().accept(this);
	        writeIndented(")();");
	    } else {
	        writeVisibility(node);
	        write("function new");
	        writeParameterList(node);
	        // TODO: generate this in body block
	        /*
	         * if (null != node.chainedConstructorInvocation()) { write(" : ");
	         * writeMethodInvocation(node.chainedConstructorInvocation()); }
	         */
	        writeLine();
	        node.body().accept(this);
	    }
	}
	
	public void visit(CSDestructor node) {
	    // TODO: compiler warning for unsupported destructors
	}
	
	public void visit(CSMethod node) {
	    printPrecedingComments(node);
	    beginEnclosingIfDefs(node);
	    writeDoc(node);
	    writeAttributes(node);
	    writeMethodHeader(node, node.modifier());
	    write("function ");
	    writeMethodName(node);
	    writeTypeParameters(node);
	    writeParameterList(node);
	    write(" : ");
	    node.returnType().accept(this);

	    if(_currentType instanceof CSInterface) {
	    	writeLine(";");
	    } else if (node.isAbstract()) {
	        writeLine("{ throw \"abstract\"; }");
	    } else {
	        writeMethodBody(node);
	    }
	    endEnclosingIfDefs(node);
	}

	private void endEnclosingIfDefs(CSNode node) {
		for (String expression : node.enclosingIfDefs()) {
			writeIndented("#end // ");
			writeLine(expression);
		}
	}

	private void beginEnclosingIfDefs(CSNode node) {
		for (String expression : node.enclosingIfDefs()) {
			writeIndented("#if ");
			writeLine(expression);
		}
	}

	private void writeMethodHeader(CSMember member, CSMethodModifier modifiers) {
		if (!_currentType.isInterface()) {
			writeVisibility(member);
			write(methodModifier(modifiers));
		} else {
			writeIndentation();
		}
	}

	protected void writeMethodBody(CSMethod node) {
		writeLine();
		node.body().accept(this);
	}

	protected void writeMethodName(CSMethod node) {
		write(node.name());
	}
	
	public void visit(CSBlock node) {
		enterBody();
		visitList(node.statements());
		leaveBody();
	}
	
	public void visit(CSDeclarationStatement node) {		
		printPrecedingComments(node);
		
		writeIndentation();
		node.declaration().accept(this);
		writeLine(";");	
		
	}
	
	public void visit(CSDeclarationExpression node) {
		node.declaration().accept(this);
	}

	private void writeDeclaration(CSTypeReferenceExpression type, String name, CSExpression initializer) {
	    write("var ");
	    write(name);
	    write(" : ");
	    type.accept(this);
	    if (null != initializer) {
	        write(" = ");
	        initializer.accept(this);
	    }
	    writeLine(";");
	}
	
	@Override
	public void visit(CSLineComment node) {
		writeIndentedLine(node.text());
	}
	
	public void visit(CSReturnStatement node) {
		
		printPrecedingComments(node);
		
		if (null == node.expression()) {
			writeIndentedLine("return;");
		} else {
			writeIndented("return ");
			node.expression().accept(this);
			writeLine(";");
		}
	}

	private void printPrecedingComments(CSNode node) {
		printPrecedingComments(node.startPosition());
	}
	
	private void printPrecedingComments(int startPosition) {
		if (startPosition <= 0) return;
		if (_lastPrintedCommentIndex >= _comments.size()) return;
		_lastPrintedCommentIndex = printCommentsBetween(_lastPrintedCommentIndex, startPosition);
	}

	private int printCommentsBetween(int lastIndex, int endStartPosition) {		
		int endIndex = commentIndexAfter(lastIndex, endStartPosition);
		if (endIndex == -1) {
			endIndex = _comments.size();
		}
		visitList(_comments.subList(lastIndex, endIndex));
		return endIndex;
	}

	private int commentIndexAfter(int startIndex, int endStartPosition) {
		for (int i=startIndex; i<_comments.size(); ++i) {
			if (_comments.get(i).startPosition() > endStartPosition) {
				return i;
			}
		}
		return -1;
	}

	public void visit(CSIfStatement node) {
		printPrecedingComments(node);
		
		writeIndented("if (");
		node.expression().accept(this);
		writeLine(")");
		node.trueBlock().accept(this);
		if (!node.falseBlock().isEmpty()) {
			writeIndentedLine("else");
			node.falseBlock().accept(this);
		}
	}
	
	public void visit(CSLockStatement node) {
	    // TODO: compiler warning for lock statement
		node.body().accept(this);
	}
	
	public void visit(CSWhileStatement node) {
		writeBlockStatement("while", node);
	} 
	
	public void visit(CSSwitchStatement node) {
		writeIndented("switch (");
		node.expression().accept(this);
		writeLine(")");
		enterBody();
		writeLineSeparatedList(node.caseClauses());
		leaveBody();
	}
	
	public void visit(CSCaseClause node) {
	    int clauses = 0;
	    writeIndented("case ");
	    for (CSExpression e : node.expressions()) {
	        if (clauses++ > 0) {
	        write(", ");
	        }
	        e.accept(this);
	    }
	    write(":");

	    if (node.isDefault()) {
	        if (clauses > 0) {
	        writeLine();
	        }
	        writeIndented("default:");
	    }
	    writeLine();
	    node.body().accept(this);
	}
	
	public void visit(CSForEachStatement node) {
		printPrecedingComments(node);
		
		writeIndented("for (");
		node.variable().accept(this);
		write(" in ");
		node.expression().accept(this);
		writeLine(")");
		node.body().accept(this);
	}
	
	public void visit(CSForStatement node) {
	    printPrecedingComments(node);
	    /**
	     * <code>
	     * {
	     *   <initializers>
	     *   if( <condition> ) 
	     *   do 
	     *   {
	     *      <body>
	     *   } while( { <updaters>; <condition> });
	     * </code>
	     */

	    enterBody();

	    for (CSExpression initializer : node.initializers()) {
	        initializer.accept(this);
	        writeLine(";");
	    }

	    if (null != node.expression()) {
	        writeIndented("if (");
	        node.expression().accept(this);
	        writeLine(")");
	    }

	    writeIndentedLine("do");
	    node.body().accept(this);
	    writeIndented("while ( {");

	    for (CSExpression updater : node.updaters()) {
	        updater.accept(this);
	        write(";");
	    }

	    if (null != node.expression()) {
	        node.expression().accept(this);
	    } else {
	        write("true");
	    }
	    writeLine("} );");
	}
	
	public void visit(CSBreakStatement node) {
		printPrecedingComments(node);
		writeIndentedLine("break;");
	}
	
	public void visit(CSGotoStatement node) {
		printPrecedingComments(node);
		if (node.target() != null) {
			writeIndented ("/*goto case ");
			node.target().accept(this);
			write (";*/");
			writeLine ();
		}
		else
			writeIndentedLine("/*goto " + node.label() + ";*/");
	}
	
	public void visit(CSContinueStatement node) {
		printPrecedingComments(node);
		writeIndentedLine("continue;");
	}

	private void writeBlockStatement(String keyword, CSBlockStatement node) {
		printPrecedingComments(node);
		writeIndented(keyword);
		write(" (");
		node.expression().accept(this);
		write(")");
		writeLine();
		node.body().accept(this);
	}
	
	public void visit(CSDoStatement node) {
		writeIndentedLine("do");
		node.body().accept(this);
		writeIndented("while (");
		node.expression().accept(this);
		writeLine(");");
	}
	
	public void visit(CSTryStatement node) {
		printPrecedingComments(node);
		
		writeIndentedLine("try");
		node.body().accept(this);
		visitList(node.catchClauses());
		// TODO: find solution for finally in haxe
		if (null != node.finallyBlock()) {
			writeIndentedLine("finally");
			node.finallyBlock().accept(this);
		}
	}
	
	public void visit(CSCatchClause node) {
		writeIndented("catch");
		CSVariableDeclaration ex = node.exception();
		if (ex != null) {
			write(" (");
			ex.accept(this);
			write(")");
		}
		writeLine();
		node.body().accept(this);
	}
	
	public void visit(CSThrowStatement node) {
		printPrecedingComments(node);
		
		if (null == node.expression()) {
			writeIndentedLine("throw;");
		} else {
			writeIndented("throw ");
			node.expression().accept(this);
			writeLine(";");
		}
	}
	
	public void visit(CSExpressionStatement node) {
		printPrecedingComments(node);
		
		writeIndentation();
		node.expression().accept(this);
		writeLine(";");
	}
	
	public void visit(CSParenthesizedExpression node) {
		write("(");
		node.expression().accept(this);
		write(")");
	}
	
	public void visit(CSConditionalExpression node) {
		node.expression().accept(this);
		write(" ? ");
		node.trueExpression().accept(this);
		write(" : ");
		node.falseExpression().accept(this);
	}
	
	public void visit(CSInfixExpression node) {
		node.lhs().accept(this);
		write(" ");
		write(node.operator());
		write(" ");
		node.rhs().accept(this);
	}
	
	public void visit(CSPrefixExpression node) {
		write(node.operator());
		node.operand().accept(this);
	}
	
	public void visit(CSPostfixExpression node) {
		node.operand().accept(this);
		write(node.operator());
	}
	
	public void visit(CSConstructorInvocationExpression node) {
		write("new ");
		writeMethodInvocation(node);
	}
	
	public void visit(CSMethodInvocationExpression node) {
		writeMethodInvocation(node);
	}

	protected void writeMethodInvocation(CSMethodInvocationExpression node) {
		node.expression().accept(this);
		writeTypeArguments(node);
		writeParameterList(node.arguments());
	}
	
	public void visit(CSNumberLiteralExpression node) {
		write(node.token());
	}
	
	public void visit(CSUncheckedExpression node) {
		//write("unchecked(");
		node.expression().accept(this);
		//write(")");
	}
	
	public void visit(CSTypeofExpression node) {
		//write("typeof(");
		node.type().accept(this);
		//write(")");
	}
	
	public void visit(CSBoolLiteralExpression node) {
		write(Boolean.toString(node.booleanValue()));
	}
	
	public void visit(CSStringLiteralExpression node) {
		write(node.escapedValue());
	}
	
	public void visit(CSCharLiteralExpression node) {
		write(node.escapedValue());
	}
	
	public void visit(CSNullLiteralExpression node) {
		write("null");
	}
	
	public void visit(CSBaseExpression node) {
		write("super");
	}
	
	public void visit(CSThisExpression node) {
		write("this");
	}
	
	public void visit(CSArrayCreationExpression node) {
		if (node.initializer() != null) {
			write("ArrayUtils.CreateAndInit");
		} else {
		    write("ArrayUtils.Create");
		}
		
		// HACK: predetect if multidimensional array and add indicator
		if (node.elementType() instanceof CSArrayTypeReference) {
		    CSArrayTypeReference csArrayTypeReference = (CSArrayTypeReference) node
		        .elementType();
		    write(Integer.toString(csArrayTypeReference.dimensions()));
		}
		
		write("<");
		node.elementType().accept(this);
		write(">(");
		
		if (null != node.length()) {
		    node.length().accept(this);
		} else {
		    write("-1");
		}
		
		if (null != node.initializer()) {
		    write(", ");
		    node.initializer().accept(this);
		}
		
		write(")");		
	}
	
	public void visit(CSArrayInitializerExpression node) {
		write("[ ");
		writeCommaSeparatedList(filterRemovedExpressions(node.expressions()));
		write(" ]");
	}
	
	private Iterable<CSNode> filterRemovedExpressions(List<CSExpression> expressions) {
	    final ArrayList<CSNode> result = new ArrayList<CSNode>(expressions.size());
	    for (CSNode e : expressions)
	    	if (!(e instanceof CSRemovedExpression))
	    		result.add(e);
		return result;
    }

	public void visit(CSIndexedExpression node) {
		node.expression().accept(this);
		write("[");
		writeCommaSeparatedList(node.indexes());
		write("]");
	}
	
	public void visit(CSCastExpression node) {
		write("(");
		node.type().accept(this);
		write(")");
		if (null != node.expression()) {
			node.expression().accept(this);
		}
	}
	
	public void visit(CSReferenceExpression node) {
		write(node.name());
	}
	
	public void visit(CSMemberReferenceExpression node) {
		node.expression().accept(this);
		write(".");
		write(node.name());
	}

	protected void writeParameterList(CSMethodBase node) {
		List<CSVariableDeclaration> parameters = node.parameters();
		write("(");
		if (node.isVarArgs()) {
			if (parameters.size() > 1) {
				writeCommaSeparatedList(parameters.subList(0, parameters.size()-1));
				write(", ");
			}
			write("/*params*/ ");
			visit(parameters.get(parameters.size()-1));
		} else {
			writeCommaSeparatedList(parameters);
		}
		write(")");
	}
	
	protected <T extends CSNode> void writeParameterList(Iterable<T> parameters) {
		write("(");
		writeCommaSeparatedList(parameters);
		write(")");
	}
	
	public void visit(CSField node) {
		writeMemberHeader(node);
		writeFieldModifiers(node);
		writeDeclaration(node.type(), node.name(), node.initializer());
	}
	
	public void visit(CSProperty node) {
	    if (node.isIndexer()) {
	        if (node.getter() != null) {
		        writeMetaMemberHeader(node);
		        write("function __indexerGet (");
		        writeCommaSeparatedList(node.parameters());
		        write(") : ");
		        node.type().accept(this);
		        writeLine();
		        if (node.isAbstract()) {
		            writeIndentedLine("{ throw \"abstract\"; }");
		        } else {
		            node.getter().accept(this);
		        }
	        }
	
	        if (node.setter() != null) {
		        writeMetaMemberHeader(node);
		        write("function __indexerSet (");
		        writeCommaSeparatedList(node.parameters());
		        write(", value : ");
		        node.type().accept(this);
		        writeLine(") : Void");
		        if (node.isAbstract()) {
		            writeIndentedLine("{ throw \"abstract\"; }");
		        } else {
		            node.setter().accept(this);
		        }
	        }
	        // TODO: generate property? replace usages?
	    } else {
	        if (node.getter() != null) {
		        write("private function __get");
		        write(node.name());
		        write("() : ");
		        node.type().accept(this);
		        writeLine();
		        if (node.isAbstract()) {
		            writeIndentedLine("{ throw \"abstract\"; }");
		        } else {
		            node.getter().accept(this);
		        }
	        }
	
	        if (node.setter() != null) {
		        write("private function __set");
		        write(node.name());
		        write("(value : ");
		        node.type().accept(this);
		        writeLine(") : Void");
		        if (node.isAbstract()) {
		            writeIndentedLine("{ throw \"abstract\"; }");
		        } else {
		            node.setter().accept(this);
		        }
	        }
	
	        writeMetaMemberHeader(node);
	        write(" var ");
	        writeLine(node.name());
	        write("(");
	        if (node.getter() == null) {
	            write("never");
	        } else {
		        write("__get");
		        write(node.name());
	        }
	        write(",");
	        if (node.setter() == null) {
	            write("never");
	        } else {
	             write("__set");
	             write(node.name());
	        }
	        write(")");
	        write(" : ");
	        node.type().accept(this);
	        write(" ");
	    }
	}

	private void writeMemberHeader(CSMember node) {
		writeDoc(node);
		writeAttributes(node);
		writeVisibility(node);
	}
	
	public void visit(CSEvent node) {
		// TODO: i think we should ensure hxevents are not generated
	}

	private void writeMetaMemberHeader(CSMetaMember node) {
		writeDoc(node);
		writeAttributes(node);
		writeMethodHeader(node, node.modifier());
	}
	
	public void visit(CSAttribute node) {
		writeIndented("@:");
		write(node.name());
		if (!node.arguments().isEmpty()) {
			writeParameterList(node.arguments());
		}
	}
	
	@Override
	public void visit(CSLabelStatement node) {
		writeLine("/*" + node.label() + ": */");
	}
	
	@Override
	public void visit(CSDocTextOverlay node) {
		writeXmlDoc(node.text());
	}
	
	public void visit(CSDocTextNode node) {
		writeXmlDoc(xmlEscape(node.text()));
	}

	private void writeXmlDoc(final String xmldocText) {
		String[] lines = xmldocText.split("\n");
		for (int i=0; i<lines.length; ++i) {
			if (i > 0) {
				writeLine();
				writeIndentation();
			}
			writeBlock(lines[i].trim().replace("<br>", "<br />"));
		}
	}
	

	private String xmlEscape(String text) {
		return text.replaceAll("(<)(/?[^\\s][^>]*)(>)", ":lt:$2:gt:")
					.replace("<", "&lt;").replace(">", "&gt;")
					.replace(":lt:", "<")
					.replace(":gt:", ">");
		
	}
	
	public void visit(CSDocTagNode node) {
		String tagName = node.tagName();
		List<CSDocAttributeNode> attributes = node.attributes();
		List<CSDocNode> fragments = node.fragments();
		
		write("<");
		write(tagName);
		if (!attributes.isEmpty()) {
			for (CSDocAttributeNode attr : attributes) {
				write(" ");
				write(attr.name());
				write("=\"");
				write(attr.value());
				write("\"");
			}
		}
		write(">");
		
		if (fragments.size() > 1) {
			writeLine();
			for (CSDocNode f : fragments) {
				writeIndentation();
				f.accept(this);
				writeLine();
			}
			writeIndented("</" + tagName + ">");
		} else {
			if (!fragments.isEmpty()) {
				fragments.get(0).accept(this);
			}
			write("</" + tagName + ">");
		}
	}
	
	private void writeAttributes(CSMember node) {
		visitList(node.attributes());
	}
	
	private void writeFieldModifiers(CSField node) {
		for (CSFieldModifier m : node.modifiers()) {
			write(m.toString().toLowerCase());
			write(" ");
		}
	}
	
	private void writeDoc(CSMember node) {
		List<CSDocNode> docs = node.docs();
		if (docs.isEmpty()) {
			return;
		}
		
		linePrefix("/// ");
		for (CSDocNode doc : docs) {
			writeIndentation();
			doc.accept(this);
			writeLine();
		}
		linePrefix(null);
	}

	private String methodModifier(CSMethodModifier modifier) {
		switch (modifier) {
		case Static: return "static ";
		case Virtual: return "";
		case Abstract: return "/*abstract*/ ";
		case AbstractOverride: return "/*abstract*/ override ";
		case Sealed: return "/*sealed*/ override ";
		case Override: return "override ";
		}
		return "";
	}
	
	interface Closure {
		void execute();
	}

	private <T extends CSNode> void writeLineSeparatedList(Iterable<T> nodes) {
		writeSeparatedList(nodes, new Closure() {
			public void execute() {
				writeLine();
			}
		});
	}
	
	private <T extends CSNode> void writeCommaSeparatedList(Iterable<T> nodes) {
		writeList(nodes, ", ");
	}
	
	private <T extends CSNode> void writeList(Iterable<T> nodes, final String separator) {
		writeSeparatedList(nodes, new Closure() {
			public void execute() {
				write(separator);
			}
		});
	}
	
	private <T extends CSNode> void writeSeparatedList(Iterable<T> nodes, Closure separator) {
		Iterator<T> iterator = nodes.iterator();
		if (!iterator.hasNext()) return;
		iterator.next().accept(this);
		while (iterator.hasNext()) {
			separator.execute();
			iterator.next().accept(this);
		}
	}
	
	private String classModifier(CSClassModifier modifier) {
		switch (modifier) {
		case Abstract: return "/*abstract*/ ";
		case Sealed: return "/*sealed*/ ";
		}
		return "";
	}
	
    protected void enterBody() {
//		writeLine();
        writeIndentedLine("{");
        indent();
	}

    private void indent() {
    	_writer.indent();
	}
    
    private void outdent() {
    	_writer.outdent();
	}
    
    private void writeIndentation() {
		_writer.writeIndentation();
	}
    
    private void writeIndented(String s) {
		_writer.writeIndented(s);
	}

	private void writeIndentedLine(String s) {
		_writer.writeIndentedLine(s);
	}
	
	private void write(String s) {
		_writer.write(s);
	}
	
	private void linePrefix(String s) {
    	_writer.linePrefix(s);
	}
	
	private void writeBlock(String s) {
    	_writer.writeBlock(s);
	}
	
	private void writeLine(String s) {
    	_writer.writeLine(s);
	}

	private void writeLine() {
    	_writer.writeLine();
	}

	protected void leaveBody() {
		outdent();
        writeIndentedLine("}");
	}
	
	class CSharpTypeReferenceVisitor extends CSVisitor {
		private CSVisitor _delegate;
		
		CSharpTypeReferenceVisitor(CSVisitor delegate) {
			_delegate = delegate;
		}
		
		@Override
		public void visit(CSArrayTypeReference node) {
			node.elementType().accept(_delegate);
		}
		
		public void visit(CSTypeReferenceExpression node) {
			node.accept(_delegate);
		}
		
		public void visit(CSTypeReference node) {
			node.accept(_delegate);
		}
	}	
}
