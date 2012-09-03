/*
 * This file is part of java2haxe.
 *
 *  java2haxe is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  alphaTab is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with java2haxe.  If not, see <http://www.gnu.org/licenses/>.
 */
package java2haxe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java2haxe.transformer.JavaStringReplaceTransformer;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.gmt.modisco.java.AbstractMethodDeclaration;
import org.eclipse.gmt.modisco.java.AbstractTypeDeclaration;
import org.eclipse.gmt.modisco.java.AnnotationTypeDeclaration;
import org.eclipse.gmt.modisco.java.BodyDeclaration;
import org.eclipse.gmt.modisco.java.ClassDeclaration;
import org.eclipse.gmt.modisco.java.CompilationUnit;
import org.eclipse.gmt.modisco.java.ConstructorDeclaration;
import org.eclipse.gmt.modisco.java.EnumConstantDeclaration;
import org.eclipse.gmt.modisco.java.EnumDeclaration;
import org.eclipse.gmt.modisco.java.FieldDeclaration;
import org.eclipse.gmt.modisco.java.ImportDeclaration;
import org.eclipse.gmt.modisco.java.Initializer;
import org.eclipse.gmt.modisco.java.InterfaceDeclaration;
import org.eclipse.gmt.modisco.java.MethodDeclaration;
import org.eclipse.gmt.modisco.java.Model;
import org.eclipse.gmt.modisco.java.NamedElement;
import org.eclipse.gmt.modisco.java.Package;
import org.eclipse.gmt.modisco.java.VariableDeclarationFragment;
import org.eclipse.gmt.modisco.java.VisibilityKind;
import org.eclipse.gmt.modisco.java.emf.JavaPackage;

/**
 * This processor takes the path to an XMI file created by MoDisco and an output
 * directory. Using the {@link Model} serialized in the XMI plain Haxe source is
 * generated.
 * 
 * TODO: Maybe we should replace this processor with an Xtend class.
 */
public class MoDiscoXmiToHaxeProcessor extends StatementSupport
{
    private String                  _inputXmi;
    private String                  _targetDir;

    private int                     _initializerCount;

    private List<IModelTransformer> _transformers = new ArrayList<>();

    public String getInputXmi()
    {
        return _inputXmi;
    }

    public void setInputXmi(String inputXmi)
    {
        _inputXmi = inputXmi;
    }

    public String getTargetDir()
    {
        return _targetDir;
    }

    public void setTargetDir(String targetDir)
    {
        _targetDir = targetDir;
        if (!_targetDir.endsWith("/"))
        {
            _targetDir += "/";
        }
    }

    public MoDiscoXmiToHaxeProcessor()
    {
        // TODO:
        _transformers.add(new JavaStringReplaceTransformer());
        // - remove anonymous types,
        // - replace method overloads
        // - move nested types
        // - move static methods and fields of interfaces to helper class
        // - transform enumerations with arguments to classes with static
        // - change java.lang.String to primitivetype string
        // - replace multiple constructors with static create methods and
        // overloads
        // - map java runtime classes to custom runtime (like sharpen)
        // members and subclasses
        // - etc.
    }

    /**
     * Starts the conversion
     * 
     * @throws IOException
     */
    public void run() throws IOException
    {
        prepareEnvironment();

        Model model = loadModel();
        EcoreUtil.resolveAll(model);

        transformModel(model);

        for (CompilationUnit unit : model.getCompilationUnits())
        {
            processCompilationUnit(unit);
        }
    }

    /**
     * applies several transformations to the java model to ensure Haxe source
     * can be generated
     * 
     * @param model
     */
    private void transformModel(Model model)
    {
        for (IModelTransformer transformer : _transformers)
        {
            transformer.transform(this, model);
        }
    }

    /**
     * Process a single {@link CompilationUnit}.
     * 
     * @param unit
     */
    private void processCompilationUnit(CompilationUnit unit)
    {
        System.out.println("processing '" + unit.getName() + "'");
        String directory = getDirectory(unit.getPackage());
        File targetFile = new File(directory, unit.getName().replace(".java",
                ".hx"));

        if (!targetFile.getParentFile().exists())
        {
            targetFile.getParentFile().mkdirs();
        }

        setImportedTypes(new HashSet<NamedElement>());
        setCurrentPackage(unit.getPackage());

        HaxeWriter out = null;
        try
        {
            out = new HaxeWriter(targetFile);

            // copy comments
            printComments(out, unit.getComments());

            // write package
            out.print("package ");
            printPackage(out, unit.getPackage());
            out.println(";");
            out.println();

            // write imports
            for (ImportDeclaration imp : unit.getImports())
            {
                printImport(out, imp);
            }
            // TODO: Add import for multidimensional arrays

            if (unit.getImports().size() > 0)
            {
                out.println();
            }

            for (AbstractTypeDeclaration type : unit.getTypes())
            {
                printAbstractTypeDeclaration(out, type);
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (out != null)
            {
                out.close();
            }
        }
    }

    //
    // print handlers for types
    //

    private void printAbstractTypeDeclaration(HaxeWriter out,
            AbstractTypeDeclaration type)
    {
        _initializerCount = 0;
        // javadoc
        printComments(out, type.getComments());

        if (type instanceof AnnotationTypeDeclaration)
        {
            printAnnotationTypeDeclaration(out,
                    ((AnnotationTypeDeclaration) type));
        }
        else if (type instanceof EnumDeclaration)
        {
            printEnumDeclaration(out, (EnumDeclaration) type);
        }
        else if (type instanceof InterfaceDeclaration)
        {
            printInterfaceDeclaration(out, (InterfaceDeclaration) type);
        }
        else if (type instanceof ClassDeclaration)
        {
            printClassDeclaration(out, (ClassDeclaration) type);
        }
    }

    private void printClassDeclaration(HaxeWriter out, ClassDeclaration type)
    {
        System.out.println("  [class] " + type.getName());
        // type name
        out.printIndent();
        out.print("class ");
        out.print(type.getName());

        // type parameters
        printTypeParameters(out, type.getTypeParameters());
        out.println();

        // base types
        out.indent();

        if (type.getSuperClass() != null)
        {
            out.printIndent();
            out.print("extends ");
            printTypeAccess(out, type.getSuperClass());
        }

        if (type.getSuperInterfaces().size() > 0)
        {
            for (int i = 0; i < type.getSuperInterfaces().size(); i++)
            {
                if (i > 0 || type.getSuperClass() != null)
                {
                    out.println(",");
                }
                out.printIndent();
                out.print("implements ");
                printTypeAccess(out, type.getSuperInterfaces().get(i));
            }
        }
        out.println();
        out.outend();

        // class body
        out.printIndent();
        out.println("{");
        out.indent();

        for (int i = 0; i < type.getBodyDeclarations().size(); i++)
        {
            printBodyDeclaration(out, type.getBodyDeclarations().get(i));
            out.println();
        }

        out.outend();
        out.printIndent();
        out.print("}");
    }

    private void printInterfaceDeclaration(HaxeWriter out,
            InterfaceDeclaration type)
    {
        System.out.println("  [interface] " + type.getName());

        // type name
        out.printIndent();
        out.print("interface ");
        out.print(type.getName());

        // type parameters
        printTypeParameters(out, type.getTypeParameters());
        out.println();

        // base types
        out.indent();
        if (type.getSuperInterfaces().size() > 0)
        {
            for (int i = 0; i < type.getSuperInterfaces().size(); i++)
            {
                if (i > 0)
                {
                    out.println(",");
                }
                out.printIndent();
                out.print("implements ");
                printTypeAccess(out, type.getSuperInterfaces().get(i));
            }
            out.println();
        }
        out.outend();

        // class body
        out.printIndent();
        out.print("{");
        out.indent();

        for (int i = 0; i < type.getBodyDeclarations().size(); i++)
        {
            printBodyDeclaration(out, type.getBodyDeclarations().get(i));
            out.println();
        }

        out.outend();
        out.printIndent();
        out.print("}");
    }

    private void printBodyDeclaration(HaxeWriter out,
            BodyDeclaration bodyDeclaration)
    {
        if (bodyDeclaration instanceof ConstructorDeclaration)
        {
            printConstructorDeclaration(out,
                    (ConstructorDeclaration) bodyDeclaration);
        }
        else if (bodyDeclaration instanceof MethodDeclaration)
        {
            printMethodDeclaration(out, (MethodDeclaration) bodyDeclaration);
        }
        else if (bodyDeclaration instanceof AbstractTypeDeclaration)
        {
            error(out, "Nested types are not supported!", false);
            printAbstractTypeDeclaration(out,
                    (AbstractTypeDeclaration) bodyDeclaration);
        }
        else if (bodyDeclaration instanceof AnnotationTypeDeclaration)
        {
            error(out, "Nested types are not supported!", false);
            printAnnotationTypeDeclaration(out,
                    (AnnotationTypeDeclaration) bodyDeclaration);
        }
        else if (bodyDeclaration instanceof Initializer)
        {
            printInitializer(out, (Initializer) bodyDeclaration);
        }
        else if (bodyDeclaration instanceof EnumConstantDeclaration)
        {
            printEnumConstantDeclaration(out,
                    (EnumConstantDeclaration) bodyDeclaration);
        }
        else if (bodyDeclaration instanceof FieldDeclaration)
        {
            printFieldDeclaration(out, (FieldDeclaration) bodyDeclaration);
        }
    }

    private void printFieldDeclaration(HaxeWriter out, FieldDeclaration field)
    {
        if (field.getFragments().size() == 1
                && field.getFragments().get(0).getName()
                        .equals("serialVersionUID"))
        {
            return;
        }
        printComments(out, field.getComments());

        for (VariableDeclarationFragment fragment : field.getFragments())
        {
            out.printIndent();

            switch (field.getModifier().getVisibility())
            {
                case PRIVATE:
                case PROTECTED:
                    out.print("private ");
                    break;
                case NONE:
                case PUBLIC:
                    out.print("public ");
                    break;
                default:
                    break;
            }

            if (field.getModifier().isStatic())
            {
                out.print("static ");
            }

            out.print("var ");
            out.print(fragment.getName());
            out.print(" : ");
            printTypeAccess(out, field.getType());

            if (fragment.getInitializer() != null)
            {
                out.print(" = ");
                printExpression(out, fragment.getInitializer());
            }
            out.println(";");
        }
    }

    private void printEnumConstantDeclaration(HaxeWriter out,
            EnumConstantDeclaration enumConstant)
    {
        printComments(out, enumConstant.getComments());
        out.printIndent();
        out.print(enumConstant.getName());
        out.println(";");
    }

    private void printInitializer(HaxeWriter out, Initializer init)
    {
        printComments(out, init.getComments());
        out.printIndent();
        if (init.getModifier().isStatic())
        {
            out.print("static ");
        }
        out.print("var __init");

        if (_initializerCount > 0) // support for multiple initializers
        {
            out.print(_initializerCount);
        }

        out.println(" = (function()");

        printBlock(out, init.getBody());

        out.printIndent();
        out.println(")();");

        _initializerCount++;
    }

    private void printMethodDeclaration(HaxeWriter out, MethodDeclaration method)
    {
        printAbstractMethodDeclaration(out, method, method.getName());
    }

    private void printConstructorDeclaration(HaxeWriter out,
            ConstructorDeclaration ctor)
    {
        if (ctor.getAbstractTypeDeclaration() instanceof InterfaceDeclaration)
        {
            error(out, "interfaces cannot contain constructor declarations",
                    false);
        }

        printAbstractMethodDeclaration(out, ctor, "new");
    }

    private void printAbstractMethodDeclaration(HaxeWriter out,
            AbstractMethodDeclaration method, String methodName)
    {
        boolean isInterface = (method.getAbstractTypeDeclaration() instanceof InterfaceDeclaration);
        printComments(out, method.getComments());

        out.printIndent();

        if (!isInterface)
        {
            VisibilityKind visibilityKind = method.getModifier() == null ? VisibilityKind.PUBLIC
                    : method.getModifier().getVisibility();
            switch (visibilityKind)
            {
                case PRIVATE:
                case PROTECTED:
                    out.print("private ");
                    break;
                case NONE:
                case PUBLIC:
                    out.print("public ");
                    break;
                default:
                    break;
            }
        }

        if (method.getModifier() != null)
        {
            if (method.getModifier().isStatic())
            {
                if (isInterface)
                {
                    error(out, "interfaces cannot contain static methods", true);
                }

                out.print("static ");
            }

            switch (method.getModifier().getInheritance())
            {
                case ABSTRACT:
                    warn(out, "Abstract methods not supported", true);
                    break;
                default:
                    break;
            }
        }

        out.print("function ");
        out.print(methodName);

        if (method.getTypeParameters().size() > 0)
        {
            printTypeParameters(out, method.getTypeParameters());
        }

        out.print("(");

        for (int i = 0; i < method.getParameters().size(); i++)
        {
            if (i > 0)
            {
                out.print(", ");
            }

            printSingleVariableDeclaration(out, method.getParameters().get(i));
        }

        out.print(")");

        if (method instanceof MethodDeclaration)
        {
            out.print(" : ");
            MethodDeclaration methodDeclaration = (MethodDeclaration) method;
            printTypeAccess(out, methodDeclaration.getReturnType());
        }

        if (isInterface)
        {
            out.println(";");
        }
        else if (method.getBody() == null)
        {
            out.println();
            out.printIndent();
            out.println("{");
            out.printIndent();
            out.println("}");
        }
        else
        {
            out.println();
            printBlock(out, method.getBody());
        }
    }

    private void printEnumDeclaration(HaxeWriter out, EnumDeclaration type)
    {
        System.out.println("  [enum] " + type.getName());
        out.print("// TODO: print enums (");
        printAbstractTypeDeclarationName(out, type);
        out.println(")");
    }

    private void printAnnotationTypeDeclaration(HaxeWriter out,
            AnnotationTypeDeclaration annotationTypeDeclaration)
    {
        System.out.println("INFO: annotation declaration found, ignored");
    }

    private void printImport(HaxeWriter out, ImportDeclaration imp)
    {
        if (imp.isStatic())
        {
            warn(out, "static imports are not supported!", false);
            return;
        }

        if (imp.getImportedElement() != null)
        {
            out.print("import ");
            printNamedElementName(out, imp.getImportedElement());
            out.println(";");
            getImportedTypes().add(imp.getImportedElement());
        }

    }

    private String getDirectory(Package pkg)
    {
        if (pkg.getPackage() == null)
        {
            return _targetDir + pkg.getName();
        }
        else
        {
            return getDirectory(pkg.getPackage()) + "/" + pkg.getName();
        }
    }

    private Model loadModel()
    {
        JavaPackage.eINSTANCE.eClass();

        Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
        Map<String, Object> m = reg.getExtensionToFactoryMap();
        m.put(Resource.Factory.Registry.DEFAULT_EXTENSION,
                new XMIResourceFactoryImpl());

        ResourceSet resSet = new ResourceSetImpl();
        Resource resource = resSet.getResource(URI.createFileURI(_inputXmi),
                true);

        Model javaModel = (Model) resource.getContents().get(0);
        return javaModel;
    }

    private void prepareEnvironment() throws IOException

    {
        File inputXmi = new File(_inputXmi);
        File targetdir = new File(_targetDir);
        if (!inputXmi.exists())
        {
            throw new FileNotFoundException("input does not exist: "
                    + inputXmi.getAbsolutePath());
        }

        if (targetdir.exists() && targetdir.isFile()
                && !targetdir.isDirectory())
        {
            throw new FileNotFoundException(
                    "the specified target does exist but is a file and not a directory");
        }

        if (targetdir.exists())
        {
            delete(targetdir);
        }

        targetdir.mkdirs();
    }

    private void delete(File f) throws IOException
    {
        if (f.isDirectory())
        {
            for (File c : f.listFiles())
            {
                delete(c);
            }
        }
        if (!f.delete())
        {
            throw new FileNotFoundException("Failed to delete file: " + f);
        }
    }

}
