package java2haxe;

import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.gmt.modisco.java.AbstractTypeDeclaration;
import org.eclipse.gmt.modisco.java.ArrayType;
import org.eclipse.gmt.modisco.java.NamedElement;
import org.eclipse.gmt.modisco.java.Package;
import org.eclipse.gmt.modisco.java.ParameterizedType;
import org.eclipse.gmt.modisco.java.PrimitiveType;
import org.eclipse.gmt.modisco.java.Type;
import org.eclipse.gmt.modisco.java.TypeAccess;
import org.eclipse.gmt.modisco.java.TypeDeclaration;
import org.eclipse.gmt.modisco.java.TypeParameter;
import org.eclipse.gmt.modisco.java.WildCardType;

/**
 * This class implements the generation of type names and type references
 */
/**
 * @author Daniel
 * 
 */
public class TypeNameSupport extends LogSupport
{
    private Package           _currentPackage;
    private Set<NamedElement> _importedTypes;

    protected Set<NamedElement> getImportedTypes()
    {
        return _importedTypes;
    }

    protected void setImportedTypes(Set<NamedElement> importedTypes)
    {
        _importedTypes = importedTypes;
    }

    protected Package getCurrentPackage()
    {
        return _currentPackage;
    }

    protected void setCurrentPackage(Package currentPackage)
    {
        _currentPackage = currentPackage;
    }

    protected void printTypeName(HaxeWriter out, Type t, boolean typeParamters)
    {
        if (t instanceof AbstractTypeDeclaration)
        {
            printAbstractTypeDeclarationName(out, (AbstractTypeDeclaration) t,
                    typeParamters);
        }
        else if (t instanceof ArrayType)
        {
            printArrayTypeName(out, (ArrayType) t);
        }
        else if (t instanceof ParameterizedType)
        {
            printParameterizedTypeName(out, (ParameterizedType) t);
        }
        else if (t instanceof PrimitiveType)
        {
            printPrimitiveType(out, (PrimitiveType) t);
        }
        else if (t instanceof WildCardType)
        {
            printWildcardType(out, (WildCardType) t);
        }
    }

    protected void printAbstractTypeDeclarationName(HaxeWriter out,
            AbstractTypeDeclaration element)
    {
        printAbstractTypeDeclarationName(out, element, false);
    }

    protected void printAbstractTypeDeclarationName(HaxeWriter out,
            AbstractTypeDeclaration element, boolean includeTypeParams)
    {
        // package
        if (element.getPackage() != _currentPackage
                && !_importedTypes.contains(element))
        {
            if (element.getPackage() != null)
            {
                printPackage(out, element.getPackage());
                out.print(".");
            }
            // nested types
            else if (element.getAbstractTypeDeclaration() != null)
            {
                warn(out, "nested type detected!", true);
                printAbstractTypeDeclarationName(out,
                        element.getAbstractTypeDeclaration());
                out.print(".");
            }
        }

        out.print(element.getName());

        if (includeTypeParams && (element instanceof TypeDeclaration))
        {
            TypeDeclaration typeDeclaration = (TypeDeclaration) element;
            printTypeParameters(out, typeDeclaration.getTypeParameters());
        }
    }

    protected void printArrayTypeName(HaxeWriter out, ArrayType t)
    {
        if (t.getDimensions() == 1)
        {
            out.print("Array<");
            printTypeAccess(out, t.getElementType());
            out.print(">");
        }
        else
        {
            // 2DArray, 3DArray,...
            out.print(t.getDimensions());
            out.print("DArray<");
            printTypeAccess(out, t.getElementType());
            out.print(">");
        }
    }

    protected void printParameterizedTypeName(HaxeWriter out,
            ParameterizedType t)
    {
        printTypeAccess(out, t.getType(), false);

        printTypeArguments(out, t.getTypeArguments());

    }

    protected void printTypeArguments(HaxeWriter out,
            EList<TypeAccess> typeArguments)
    {
        if (typeArguments.size() > 0)
        {
            out.print("<");

            for (int i = 0; i < typeArguments.size(); i++)
            {
                if (i > 0)
                {
                    out.print(", ");
                }
                printTypeAccess(out, typeArguments.get(i));
            }

            out.print(">");
        }
    }

    protected void printPrimitiveType(HaxeWriter out, PrimitiveType t)
    {
        if (t.getName().equals("byte"))
        {
            warn(out, "byte not supported", true);
            out.print("Byte");
        }
        else if (t.getName().equals("short"))
        {
            warn(out, "short not supported", true);
            out.print("Short");
        }
        else if (t.getName().equals("int"))
        {
            out.print("Int");
        }
        else if (t.getName().equals("long"))
        {
            warn(out, "long not supported", true);
            out.print("Int");
        }
        else if (t.getName().equals("float"))
        {
            out.print("Float");
        }
        else if (t.getName().equals("double"))
        {
            out.print("Float");
        }
        else if (t.getName().equals("boolean"))
        {
            out.print("Bool");
        }
        else if (t.getName().equals("char"))
        {
            warn(out, "char not supported", true);
            out.print("Int");
        }
        else if (t.getName().equals("void"))
        {
            out.print("Void");
        }
        else
        {
            out.print(t.getName());
        }
    }

    protected void printWildcardType(HaxeWriter out, WildCardType t)
    {
        error(out, "wildcard types are not supported", false);
        out.print("dynamic");
    }

    protected void printTypeParameters(HaxeWriter out,
            EList<TypeParameter> typeParameters)
    {
        if (typeParameters.size() > 0)
        {
            out.print("<");
            for (int i = 0; i < typeParameters.size(); i++)
            {
                if (i > 0)
                {
                    out.print(", ");
                }
                TypeParameter p = typeParameters.get(i);
                out.print(p.getName());
                if (p.getBounds().size() > 0)
                {
                    out.print(" : (");

                    for (int j = 0; j < p.getBounds().size(); j++)
                    {
                        if (j > 0)
                        {
                            out.print(", ");
                        }

                        printTypeAccess(out, p.getBounds().get(j));
                    }

                    out.print(")");
                }
            }
            out.print(">");
        }
    }

    protected void printTypeAccess(HaxeWriter out, TypeAccess typeAccess)
    {
        printTypeAccess(out, typeAccess, false);
    }

    protected void printTypeAccess(HaxeWriter out, TypeAccess typeAccess,
            boolean typeParameters)
    {
        printTypeName(out, typeAccess.getType(), typeParameters);
    }

    protected void printNamedElementName(HaxeWriter out, NamedElement element)
    {
        if (element instanceof AbstractTypeDeclaration)
        {
            AbstractTypeDeclaration abstractTypeDeclaration = (AbstractTypeDeclaration) element;
            printAbstractTypeDeclarationName(out, abstractTypeDeclaration);
        }
        else
        {
            out.print(element.getName());
        }
    }

    protected void printPackage(HaxeWriter out, Package package1)
    {
        if (package1.getPackage() == null)
        {
            out.print(package1.getName());
        }
        else
        {
            printPackage(out, package1.getPackage());
            out.print(".");
            out.print(package1.getName());
        }
    }
}
