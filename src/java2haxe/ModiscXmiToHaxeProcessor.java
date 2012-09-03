package java2haxe;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.gmt.modisco.java.AbstractMethodDeclaration;
import org.eclipse.gmt.modisco.java.AbstractMethodInvocation;
import org.eclipse.gmt.modisco.java.AbstractTypeDeclaration;
import org.eclipse.gmt.modisco.java.Annotation;
import org.eclipse.gmt.modisco.java.AnnotationTypeDeclaration;
import org.eclipse.gmt.modisco.java.ArrayAccess;
import org.eclipse.gmt.modisco.java.ArrayCreation;
import org.eclipse.gmt.modisco.java.ArrayInitializer;
import org.eclipse.gmt.modisco.java.ArrayLengthAccess;
import org.eclipse.gmt.modisco.java.ArrayType;
import org.eclipse.gmt.modisco.java.AssertStatement;
import org.eclipse.gmt.modisco.java.Assignment;
import org.eclipse.gmt.modisco.java.Block;
import org.eclipse.gmt.modisco.java.BodyDeclaration;
import org.eclipse.gmt.modisco.java.BooleanLiteral;
import org.eclipse.gmt.modisco.java.BreakStatement;
import org.eclipse.gmt.modisco.java.CastExpression;
import org.eclipse.gmt.modisco.java.CatchClause;
import org.eclipse.gmt.modisco.java.CharacterLiteral;
import org.eclipse.gmt.modisco.java.ClassDeclaration;
import org.eclipse.gmt.modisco.java.ClassInstanceCreation;
import org.eclipse.gmt.modisco.java.Comment;
import org.eclipse.gmt.modisco.java.CompilationUnit;
import org.eclipse.gmt.modisco.java.ConditionalExpression;
import org.eclipse.gmt.modisco.java.ConstructorDeclaration;
import org.eclipse.gmt.modisco.java.ConstructorInvocation;
import org.eclipse.gmt.modisco.java.ContinueStatement;
import org.eclipse.gmt.modisco.java.DoStatement;
import org.eclipse.gmt.modisco.java.EmptyStatement;
import org.eclipse.gmt.modisco.java.EnhancedForStatement;
import org.eclipse.gmt.modisco.java.EnumConstantDeclaration;
import org.eclipse.gmt.modisco.java.EnumDeclaration;
import org.eclipse.gmt.modisco.java.Expression;
import org.eclipse.gmt.modisco.java.ExpressionStatement;
import org.eclipse.gmt.modisco.java.FieldAccess;
import org.eclipse.gmt.modisco.java.FieldDeclaration;
import org.eclipse.gmt.modisco.java.ForStatement;
import org.eclipse.gmt.modisco.java.IfStatement;
import org.eclipse.gmt.modisco.java.ImportDeclaration;
import org.eclipse.gmt.modisco.java.InfixExpression;
import org.eclipse.gmt.modisco.java.Initializer;
import org.eclipse.gmt.modisco.java.InstanceofExpression;
import org.eclipse.gmt.modisco.java.InterfaceDeclaration;
import org.eclipse.gmt.modisco.java.LabeledStatement;
import org.eclipse.gmt.modisco.java.MethodDeclaration;
import org.eclipse.gmt.modisco.java.MethodInvocation;
import org.eclipse.gmt.modisco.java.Model;
import org.eclipse.gmt.modisco.java.NamedElement;
import org.eclipse.gmt.modisco.java.NullLiteral;
import org.eclipse.gmt.modisco.java.NumberLiteral;
import org.eclipse.gmt.modisco.java.Package;
import org.eclipse.gmt.modisco.java.ParameterizedType;
import org.eclipse.gmt.modisco.java.ParenthesizedExpression;
import org.eclipse.gmt.modisco.java.PostfixExpression;
import org.eclipse.gmt.modisco.java.PrefixExpression;
import org.eclipse.gmt.modisco.java.PrimitiveType;
import org.eclipse.gmt.modisco.java.ReturnStatement;
import org.eclipse.gmt.modisco.java.SingleVariableAccess;
import org.eclipse.gmt.modisco.java.SingleVariableDeclaration;
import org.eclipse.gmt.modisco.java.Statement;
import org.eclipse.gmt.modisco.java.StringLiteral;
import org.eclipse.gmt.modisco.java.SuperConstructorInvocation;
import org.eclipse.gmt.modisco.java.SuperFieldAccess;
import org.eclipse.gmt.modisco.java.SuperMethodInvocation;
import org.eclipse.gmt.modisco.java.SwitchCase;
import org.eclipse.gmt.modisco.java.SwitchStatement;
import org.eclipse.gmt.modisco.java.SynchronizedStatement;
import org.eclipse.gmt.modisco.java.ThisExpression;
import org.eclipse.gmt.modisco.java.ThrowStatement;
import org.eclipse.gmt.modisco.java.TryStatement;
import org.eclipse.gmt.modisco.java.Type;
import org.eclipse.gmt.modisco.java.TypeAccess;
import org.eclipse.gmt.modisco.java.TypeDeclaration;
import org.eclipse.gmt.modisco.java.TypeDeclarationStatement;
import org.eclipse.gmt.modisco.java.TypeLiteral;
import org.eclipse.gmt.modisco.java.TypeParameter;
import org.eclipse.gmt.modisco.java.VariableDeclarationExpression;
import org.eclipse.gmt.modisco.java.VariableDeclarationFragment;
import org.eclipse.gmt.modisco.java.VariableDeclarationStatement;
import org.eclipse.gmt.modisco.java.VisibilityKind;
import org.eclipse.gmt.modisco.java.WhileStatement;
import org.eclipse.gmt.modisco.java.WildCardType;
import org.eclipse.gmt.modisco.java.emf.JavaFactory;
import org.eclipse.gmt.modisco.java.emf.JavaPackage;

/**
 * This processor takes the path to an XMI file created by Modisc and an output
 * directory. Using the {@link Model} serialized in the XMI plain Haxe source is
 * generated.
 * 
 * TODO: Maybe we should replace this processor with an Xtend class.
 */
public class ModiscXmiToHaxeProcessor
{
    private String            _inputXmi;
    private String            _targetDir;

    private Package           _currentPackage;
    private int               _initializerCount;

    private Set<NamedElement> _importedTypes;

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

    // some very basic logging

    private void warn(HaxeWriter out, String s, boolean inline)
    {
        log(out, "WARN: " + s, inline);
    }

    private void error(HaxeWriter out, String s, boolean inline)
    {
        log(out, "ERR: " + s, inline);
    }

    private void log(HaxeWriter out, String s, boolean inline)
    {
        if (out != null)
        {
            if (inline)
            {
                out.print("/* " + s + " */ ");
            }
            else
            {
                out.println();
                out.printIndent();
                out.println("/* " + s + " */");
            }
        }
        System.out.println(s);
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
        // TODO: - remove anonymous types,
        // - replace method overloads
        // - move nested types
        // - move static methods and fields of interfaces to helper class
        // - transform enumerations with arguments to classes with static
        // - change java.lang.String to primitivetype string
        // - replace multiple constructors with static create methods and
        // overloads
        replaceJavaString(model);
        // - map java runtime classes to custom runtime (like sharpen)
        // members and subclasses
        // - etc.
    }

    /**
     * Replaces the ocurrences of java.lang.String by a new primitive type
     * String.
     * 
     * @param model
     */
    private void replaceJavaString(Model model)
    {
        Type t = findType(model, "java.lang.String");

        if (t == null)
        {
            warn(null, "could not find java.lang.String", false);
        }
        else
        {
            PrimitiveType newString = JavaFactory.eINSTANCE
                    .createPrimitiveType();
            newString.setName("String");

            for (ImportDeclaration imp : t.getUsagesInImports().toArray(
                    new ImportDeclaration[0]))
            {
                imp.setImportedElement(newString);
            }

            for (TypeAccess ta : t.getUsagesInTypeAccess().toArray(
                    new TypeAccess[0]))
            {
                ta.setType(newString);
            }
        }
    }

    /**
     * finds a type within the model using the given full qualified name
     * 
     * @param model
     *            the model to search the type in
     * @param fqn
     *            the fully qualified name seperated by dots
     * @return the type found in the given model or <code>null</code> if the
     *         type couldn't be found
     */
    private Type findType(Model model, String fqn)
    {
        String[] token = fqn.split("\\.");

        Package pkg = null;
        for (int i = 0; i < token.length; i++)
        {
            String t = token[i];
            // are we in root?
            if (pkg == null)
            {
                // search package within root
                for (Package p : model.getOwnedElements())
                {
                    if (p.getName().equals(t))
                    {
                        pkg = p;
                        break;
                    }
                }

                // no package found
                if (pkg == null)
                {
                    return null;
                }
            }
            // searching subpackages?
            else if (i < token.length - 1)
            {
                // reset current package to check if we found one
                Package o = pkg;
                pkg = null;
                for (Package p : o.getOwnedPackages())
                {
                    if (p.getName().equals(t))
                    {
                        pkg = p;
                        break;
                    }
                }

                // subpackage not found
                if (pkg == null)
                {
                    return null;
                }
            }
            // searching type in package
            else if (pkg != null)
            {
                for (AbstractTypeDeclaration type : pkg.getOwnedElements())
                {
                    if (type.getName().equals(t))
                    {
                        return type;
                    }
                }

                return null;
            }
        }

        return null;
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

        _importedTypes = new HashSet<>();
        _currentPackage = unit.getPackage();

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

    private void printSingleVariableDeclaration(HaxeWriter out,
            SingleVariableDeclaration singleVariableDeclaration)
    {
        if (singleVariableDeclaration.isVarargs())
        {
            out.print(singleVariableDeclaration.getName());
            out.print(" : Array<");
            printTypeAccess(out, singleVariableDeclaration.getType());
            out.print(">");
        }
        else
        {
            out.print(singleVariableDeclaration.getName());
            out.print(" : ");
            printTypeAccess(out, singleVariableDeclaration.getType());
        }
    }

    private void printComments(HaxeWriter out, EList<Comment> comments)
    {
        for (Comment c : comments)
        {
            printComment(out, c);
        }
    }

    private void printComment(HaxeWriter out, Comment c)
    {
        String[] lines = c.getContent().split("\\r?\\n");
        for (String l : lines)
        {
            out.printIndent();
            out.println(l);
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
            _importedTypes.add(imp.getImportedElement());
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

    //
    // Type Name Printing
    //

    private void printTypeName(HaxeWriter out, Type t, boolean typeParamters)
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

    private void printAbstractTypeDeclarationName(HaxeWriter out,
            AbstractTypeDeclaration element)
    {
        printAbstractTypeDeclarationName(out, element, false);
    }

    private void printAbstractTypeDeclarationName(HaxeWriter out,
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

    private void printArrayTypeName(HaxeWriter out, ArrayType t)
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

    private void printParameterizedTypeName(HaxeWriter out, ParameterizedType t)
    {
        printTypeAccess(out, t.getType(), false);

        printTypeArguments(out, t.getTypeArguments());

    }

    private void printTypeArguments(HaxeWriter out,
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

    private void printPrimitiveType(HaxeWriter out, PrimitiveType t)
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

    private void printWildcardType(HaxeWriter out, WildCardType t)
    {
        error(out, "wildcard types are not supported", false);
        out.print("dynamic");
    }

    private void printTypeParameters(HaxeWriter out,
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

    private void printTypeAccess(HaxeWriter out, TypeAccess typeAccess)
    {
        printTypeAccess(out, typeAccess, false);
    }

    private void printTypeAccess(HaxeWriter out, TypeAccess typeAccess,
            boolean typeParameters)
    {
        printTypeName(out, typeAccess.getType(), typeParameters);
    }

    private void printNamedElementName(HaxeWriter out, NamedElement element)
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

    private void printPackage(HaxeWriter out, Package package1)
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

    //
    // Statement Printing
    //

    private void printStatement(HaxeWriter out, Statement statement)
    {
        if (!(statement instanceof Block))
        {
            printComments(out, statement.getComments());
        }

        if (statement instanceof AssertStatement)
        {
            printAssertStatement(out, (AssertStatement) statement);
        }
        else if (statement instanceof Block)
        {
            printBlock(out, (Block) statement);
        }
        else if (statement instanceof BreakStatement)
        {
            printBreakStatement(out, (BreakStatement) statement);
        }
        else if (statement instanceof ConstructorInvocation)
        {
            printConstructorInvocation(out, (ConstructorInvocation) statement);
        }
        else if (statement instanceof ContinueStatement)
        {
            printContinueStatement(out, (ContinueStatement) statement);
        }
        else if (statement instanceof DoStatement)
        {
            printDoStatement(out, (DoStatement) statement);
        }
        else if (statement instanceof EmptyStatement)
        {
            printEmptyStatement(out, (EmptyStatement) statement);
        }
        else if (statement instanceof EnhancedForStatement)
        {
            printEnhancedForStatement(out, (EnhancedForStatement) statement);
        }
        else if (statement instanceof ExpressionStatement)
        {
            printExpressionStatement(out, (ExpressionStatement) statement);
        }
        else if (statement instanceof ForStatement)
        {
            printForStatement(out, (ForStatement) statement);
        }
        else if (statement instanceof IfStatement)
        {
            printIfStatement(out, (IfStatement) statement);
        }
        else if (statement instanceof LabeledStatement)
        {
            printLabeledStatement(out, (LabeledStatement) statement);
        }
        else if (statement instanceof ReturnStatement)
        {
            printReturnStatement(out, (ReturnStatement) statement);
        }
        else if (statement instanceof SuperConstructorInvocation)
        {
            printSuperConstructorInvocation(out,
                    (SuperConstructorInvocation) statement);
        }
        else if (statement instanceof SwitchStatement)
        {
            printSwitchStatement(out, (SwitchStatement) statement);
        }
        else if (statement instanceof SynchronizedStatement)
        {
            printSynchronizedStatement(out, (SynchronizedStatement) statement);
        }
        else if (statement instanceof ThrowStatement)
        {
            printThrowStatement(out, (ThrowStatement) statement);
        }
        else if (statement instanceof TryStatement)
        {
            printTryStatement(out, (TryStatement) statement);
        }
        else if (statement instanceof TypeDeclarationStatement)
        {
            printTypeDeclarationStatement(out,
                    (TypeDeclarationStatement) statement);
        }
        else if (statement instanceof VariableDeclarationStatement)
        {
            printVariableDeclarationStatement(out,
                    (VariableDeclarationStatement) statement);
        }
        else if (statement instanceof WhileStatement)
        {
            printWhileStatement(out, (WhileStatement) statement);
        }
    }

    private void printWhileStatement(HaxeWriter out, WhileStatement statement)
    {
        out.printIndent();
        out.print("while(");
        printExpression(out, statement.getExpression());
        out.println(")");
        if (!(statement.getBody() instanceof Block))
        {
            out.indent();
        }
        printStatement(out, statement.getBody());
        if (!(statement.getBody() instanceof Block))
        {
            out.outend();
        }
    }

    private void printVariableDeclarationStatement(HaxeWriter out,
            VariableDeclarationStatement statement)
    {
        for (VariableDeclarationFragment fragment : statement.getFragments())
        {
            out.printIndent();
            out.print("var ");
            out.print(fragment.getName());
            out.print(" : ");
            printTypeAccess(out, statement.getType());

            if (fragment.getInitializer() != null)
            {
                out.print(" = ");
                printExpression(out, fragment.getInitializer());
            }

            out.println(";");
        }
    }

    private void printTypeDeclarationStatement(HaxeWriter out,
            TypeDeclarationStatement statement)
    {
        error(out, "class declaration statements are not supported", false);
    }

    private void printTryStatement(HaxeWriter out, TryStatement statement)
    {
        out.printIndent();
        out.println("try");
        printBlock(out, statement.getBody());

        for (CatchClause c : statement.getCatchClauses())
        {
            printComments(out, c.getComments());
            out.printIndent();
            out.print("catch(");
            printSingleVariableDeclaration(out, c.getException());
            out.println(")");

            printBlock(out, c.getBody());
        }

        if (statement.getFinally() != null)
        {
            warn(out, "TODO: workaround for finally needed", false);
            out.printIndent();
            out.println("finally");
            printBlock(out, statement.getFinally());
        }
    }

    private void printThrowStatement(HaxeWriter out, ThrowStatement statement)
    {
        out.printIndent();
        out.print("throw ");
        printExpression(out, statement.getExpression());
        out.println(";");
    }

    private void printSynchronizedStatement(HaxeWriter out,
            SynchronizedStatement statement)
    {
        error(out, "Synchronized statement not supported", false);
        out.printIndent();
        out.print("/*synchronized(*/");
        printExpression(out, statement.getExpression());
        out.print("/*)*/");
        printBlock(out, statement.getBody());
    }

    private void printSwitchStatement(HaxeWriter out, SwitchStatement statement)
    {
        out.printIndent();
        out.print("switch(");
        printExpression(out, statement.getExpression());
        out.println(")");

        out.printIndent();
        out.println("{");
        out.indent();

        for (int i = 0; i < statement.getStatements().size(); i++)
        {
            Statement s = statement.getStatements().get(i);
            if (s instanceof SwitchCase)
            {
                SwitchCase switchCase = (SwitchCase) s;
                out.printIndent();
                if (switchCase.isDefault())
                {
                    out.println("default:");
                }
                else
                {
                    out.print("case ");
                    printExpression(out, switchCase.getExpression());

                    while ((i < (statement.getStatements().size() - 1))
                            && (statement.getStatements().get(i + 1) instanceof SwitchCase)
                            && !((SwitchCase) (statement.getStatements()
                                    .get(i + 1))).isDefault())
                    {
                        out.print(", ");
                        printExpression(out, ((SwitchCase) statement
                                .getStatements().get(i++)).getExpression());
                    }

                    out.println(": ");
                }
            }
            else if (s instanceof BreakStatement)
            {
                // skip break if it is the last statement or the next statement
                // is a switchcase
                if ((i < (statement.getStatements().size() - 1))
                        && !(statement.getStatements().get(i + 1) instanceof SwitchCase))
                {
                    printStatement(out, s);
                }
            }
            else
            {
                printStatement(out, s);
            }
        }

        out.outend();
        out.printIndent();
        out.println("}");

    }

    private void printSuperConstructorInvocation(HaxeWriter out,
            SuperConstructorInvocation statement)
    {
        out.printIndent();
        out.print("super(");

        for (int i = 0; i < statement.getArguments().size(); i++)
        {
            if (i > 0)
            {
                out.print(", ");
            }

            printExpression(out, statement.getArguments().get(i));
        }

        out.println(");");
    }

    private void printReturnStatement(HaxeWriter out, ReturnStatement statement)
    {
        out.printIndent();
        if (statement.getExpression() == null)
        {
            out.println("return;");
        }
        else
        {
            out.print("return ");
            printExpression(out, statement.getExpression());
            out.println(";");
        }
    }

    private void printLabeledStatement(HaxeWriter out,
            LabeledStatement statement)
    {
        error(out, "statement labels are not supported", false);
        printStatement(out, statement.getBody());
    }

    private void printIfStatement(HaxeWriter out, IfStatement statement)
    {
        out.printIndent();
        out.print("if(");
        printExpression(out, statement.getExpression());
        out.println(")");
        if (!(statement.getThenStatement() instanceof Block))
        {
            out.indent();
        }
        printStatement(out, statement.getThenStatement());
        if (!(statement.getThenStatement() instanceof Block))
        {
            out.outend();
        }

        if (statement.getElseStatement() != null)
        {
            printComments(out, statement.getElseStatement().getComments());
            out.printIndent();
            out.print("else ");
            if (statement.getElseStatement() instanceof IfStatement)
            {
                printStatement(out, statement.getElseStatement());
            }
            else if (statement.getElseStatement() instanceof Block)
            {
                out.println();
                printStatement(out, statement.getElseStatement());
            }
            else
            {
                out.println();
                out.indent();
                printStatement(out, statement.getElseStatement());
                out.outend();
            }
        }
    }

    private void printForStatement(HaxeWriter out, ForStatement statement)
    {
        // thanks to David Holan for the clean pattern of converting for loops
        /*
         * { <init>; if ( <condition> ) do {
         * 
         * <body> } while ( function(){ <increment>; return false; }() ||
         * <condition> ); }
         */
        out.printIndent();
        out.println("{");
        out.indent();
        {
            // <init>
            for (Expression e : statement.getInitializers())
            {
                out.printIndent();
                printExpression(out, e);
                out.println(";");
            }

            out.printIndent();
            out.print("if(");
            if (statement.getExpression() != null)
            {
                printExpression(out, statement.getExpression());
            }
            else
            {
                out.print(true);
            }
            out.println(") do");
            out.printIndent();

            // <body>
            out.println("{");
            out.indent();
            {
                printStatement(out, statement.getBody());
            }
            out.outend();
            out.printIndent();
            out.print("} while( function() { ");
            // <increment>
            for (Expression e : statement.getUpdaters())
            {
                printExpression(out, e);
                out.print("; ");
            }
            out.print("; return false; }() || ");
            if (statement.getExpression() != null)
            {
                printExpression(out, statement.getExpression());
            }
            else
            {
                out.print("true");
            }
            out.println(");");

        }
        out.outend();
        out.printIndent();
        out.println("}");
    }

    private void printExpressionStatement(HaxeWriter out,
            ExpressionStatement statement)
    {
        out.printIndent();
        printExpression(out, statement.getExpression());
        out.println(";");
    }

    private void printEnhancedForStatement(HaxeWriter out,
            EnhancedForStatement statement)
    {
        out.printIndent();
        out.print("for(");
        out.print(statement.getParameter().getName());
        out.print(" in ");
        printExpression(out, statement.getExpression());
        out.println(")");

        if (!(statement.getBody() instanceof Block))
        {
            out.indent();
        }
        printStatement(out, statement.getBody());
        if (!(statement.getBody() instanceof Block))
        {
            out.outend();
        }
    }

    private void printEmptyStatement(HaxeWriter out, EmptyStatement statement)
    {
        // we don't need those
    }

    private void printDoStatement(HaxeWriter out, DoStatement statement)
    {
        out.printIndent();
        out.println("do");
        if (!(statement.getBody() instanceof Block))
        {
            out.indent();
        }
        printStatement(out, statement.getBody());
        if (!(statement.getBody() instanceof Block))
        {
            out.outend();
        }
        out.print("while(");
        printExpression(out, statement.getExpression());
        out.println(");");
    }

    private void printContinueStatement(HaxeWriter out,
            ContinueStatement statement)
    {
        if (statement.getLabel() != null)
        {
            error(out, "labeled continues are not supported", false);
        }
        out.printIndent();
        out.println("continue;");
    }

    private void printConstructorInvocation(HaxeWriter out,
            ConstructorInvocation statement)
    {
        error(out, "constructor overloads are not supported", false);
        out.printIndent();
        printAbstractMethodInvokation(out, statement, "this");
        out.println(";");
    }

    private void printAbstractMethodInvokation(HaxeWriter out,
            AbstractMethodInvocation invoke, String methodName)
    {
        out.print(methodName);
        if (invoke.getTypeArguments().size() > 0)
        {
            error(out, "generic methods are not supported", false);
        }
        printTypeArguments(out, invoke.getTypeArguments());
        out.print("(");

        int paramIndex = 0;
        for (int i = 0; i < invoke.getArguments().size(); i++)
        {
            if (i > 0)
            {
                out.print(", ");
            }
            boolean hasParam = paramIndex < invoke.getMethod().getParameters()
                    .size();
            if (hasParam
                    && invoke.getMethod().getParameters().get(paramIndex)
                            .isVarargs())
            {
                out.print("Reflect.makeVarArgs(");
                printExpression(out, invoke.getArguments().get(i));
                out.print(")");
            }
            else
            {
                if (!hasParam)
                {
                    warn(out, "Detected varargs of core method", false);
                }
                printExpression(out, invoke.getArguments().get(i));
                paramIndex++;
            }
        }
        out.print(")");
    }

    private void printBreakStatement(HaxeWriter out, BreakStatement statement)
    {
        if (statement.getLabel() != null)
        {
            error(out, "labeled breaks are not supported", false);
        }
        out.printIndent();
        out.println("break;");
    }

    private void printAssertStatement(HaxeWriter out, AssertStatement statement)
    {
        error(out, "assert statements not supported", false);
    }

    private void printBlock(HaxeWriter out, Block body)
    {
        out.printIndent();
        out.println("{");
        out.indent();
        printComments(out, body.getComments());
        for (Statement stmt : body.getStatements())
        {
            printStatement(out, stmt);
        }

        out.outend();
        out.printIndent();
        out.println("}");
    }

    //
    // Expression Printing
    //

    private void printExpression(HaxeWriter out, Expression ex)
    {
        if (ex instanceof Annotation)
        {
            printAnnotation(out, (Annotation) ex);
        }
        else if (ex instanceof ArrayInitializer)
        {
            printArrayInitializer(out, (ArrayInitializer) ex);
        }
        else if (ex instanceof ArrayLengthAccess)
        {
            printArrayLengthAccess(out, (ArrayLengthAccess) ex);
        }
        else if (ex instanceof BooleanLiteral)
        {
            printBooleanLiteral(out, (BooleanLiteral) ex);
        }
        else if (ex instanceof ClassInstanceCreation)
        {
            printClassInstanceCreation(out, (ClassInstanceCreation) ex);
        }
        else if (ex instanceof ArrayAccess)
        {
            printArrayAccess(out, (ArrayAccess) ex);
        }
        else if (ex instanceof ArrayCreation)
        {
            printArrayCreation(out, (ArrayCreation) ex);
        }
        else if (ex instanceof Assignment)
        {
            printAssignment(out, (Assignment) ex);
        }
        else if (ex instanceof ConditionalExpression)
        {
            printConditionalExpression(out, (ConditionalExpression) ex);
        }
        else if (ex instanceof CastExpression)
        {
            printCastExpression(out, (CastExpression) ex);
        }
        else if (ex instanceof CharacterLiteral)
        {
            printCharacterLiteral(out, (CharacterLiteral) ex);
        }
        else if (ex instanceof InfixExpression)
        {
            printInfixExpression(out, (InfixExpression) ex);
        }
        else if (ex instanceof FieldAccess)
        {
            printFieldAccess(out, (FieldAccess) ex);
        }
        else if (ex instanceof InstanceofExpression)
        {
            printInstanceofExpression(out, (InstanceofExpression) ex);
        }
        else if (ex instanceof MethodInvocation)
        {
            printMethodInvocation(out, (MethodInvocation) ex);
        }
        else if (ex instanceof TypeLiteral)
        {
            printTypeLiteral(out, (TypeLiteral) ex);
        }
        else if (ex instanceof PostfixExpression)
        {
            printPostfixExpression(out, (PostfixExpression) ex);
        }
        else if (ex instanceof ParenthesizedExpression)
        {
            printParenthesizedExpression(out, (ParenthesizedExpression) ex);
        }
        else if (ex instanceof NumberLiteral)
        {
            printNumberLiteral(out, (NumberLiteral) ex);
        }
        else if (ex instanceof NullLiteral)
        {
            printNullLiteral(out, (NullLiteral) ex);
        }
        else if (ex instanceof PrefixExpression)
        {
            printPrefixExpression(out, (PrefixExpression) ex);
        }
        else if (ex instanceof StringLiteral)
        {
            printStringLiteral(out, (StringLiteral) ex);
        }
        else if (ex instanceof SuperFieldAccess)
        {
            printSuperFieldAccess(out, (SuperFieldAccess) ex);
        }
        else if (ex instanceof VariableDeclarationExpression)
        {
            printVariableDeclarationExpression(out,
                    (VariableDeclarationExpression) ex);
        }
        else if (ex instanceof ThisExpression)
        {
            printThisExpression(out, (ThisExpression) ex);
        }
        else if (ex instanceof SuperMethodInvocation)
        {
            printSuperMethodInvocation(out, (SuperMethodInvocation) ex);
        }
        else if (ex instanceof SingleVariableAccess)
        {
            printSingleVariableAccess(out, (SingleVariableAccess) ex);
        }
        else if (ex instanceof TypeAccess)
        {
            printTypeAccess(out, (TypeAccess) ex);
        }
    }

    private void printSingleVariableAccess(HaxeWriter out,
            SingleVariableAccess ex)
    {
        out.print(ex.getVariable().getName());
    }

    private void printSuperMethodInvocation(HaxeWriter out,
            SuperMethodInvocation ex)
    {
        out.print("super.");
        printAbstractMethodInvokation(out, ex, ex.getMethod().getName());
    }

    private void printThisExpression(HaxeWriter out, ThisExpression ex)
    {
        out.print("this");
    }

    private void printVariableDeclarationExpression(HaxeWriter out,
            VariableDeclarationExpression ex)
    {
        out.print("var ");
        for (VariableDeclarationFragment f : ex.getFragments())
        {
            out.print(f.getName());
            out.print(" : ");
            printTypeAccess(out, ex.getType());

            if (f.getInitializer() != null)
            {
                out.print(" = ");
                printExpression(out, f.getInitializer());
            }
        }
    }

    private void printSuperFieldAccess(HaxeWriter out, SuperFieldAccess ex)
    {
        out.print("super.");
        out.print(ex.getField().getVariable().getName());
    }

    private void printStringLiteral(HaxeWriter out, StringLiteral ex)
    {
        out.print("\"");
        out.print(ex.getEscapedValue());
        out.print("\"");
    }

    private void printPrefixExpression(HaxeWriter out, PrefixExpression ex)
    {
        out.print(ex.getOperator().getLiteral());
        printExpression(out, ex.getOperand());
    }

    private void printNullLiteral(HaxeWriter out, NullLiteral ex)
    {
        out.print("null");
    }

    private void printNumberLiteral(HaxeWriter out, NumberLiteral ex)
    {
        if (ex.getTokenValue().toLowerCase().endsWith("f")
                || ex.getTokenValue().toLowerCase().endsWith("l"))
        {
            out.print(ex.getTokenValue().substring(0,
                    ex.getTokenValue().length() - 1));
        }
        else
        {
            out.print(ex.getTokenValue());
        }
    }

    private void printParenthesizedExpression(HaxeWriter out,
            ParenthesizedExpression ex)
    {
        out.print("(");

        printExpression(out, ex.getExpression());

        out.print(")");
    }

    private void printPostfixExpression(HaxeWriter out, PostfixExpression ex)
    {
        printExpression(out, ex.getOperand());
        out.print(ex.getOperator().getLiteral());
    }

    private void printTypeLiteral(HaxeWriter out, TypeLiteral ex)
    {
        printTypeAccess(out, ex.getType());
    }

    private void printMethodInvocation(HaxeWriter out, MethodInvocation ex)
    {
        if (ex.getExpression() != null)
        {
            printExpression(out, ex.getExpression());
            out.print(".");
        }
        printAbstractMethodInvokation(out, ex, ex.getMethod().getName());
    }

    private void printInstanceofExpression(HaxeWriter out,
            InstanceofExpression ex)
    {
        out.print("Std.is(");
        printExpression(out, ex.getLeftOperand());
        out.print(", ");
        printTypeAccess(out, ex.getRightOperand());
        out.print(")");
    }

    private void printFieldAccess(HaxeWriter out, FieldAccess ex)
    {
        printExpression(out, ex.getExpression());
        out.print(".");
        out.print(ex.getField().getVariable().getName());
    }

    private void printInfixExpression(HaxeWriter out, InfixExpression ex)
    {
        printExpression(out, ex.getLeftOperand());
        out.print(" ");
        out.print(ex.getOperator().getLiteral());
        out.print(" ");
        printExpression(out, ex.getRightOperand());
    }

    private void printCharacterLiteral(HaxeWriter out, CharacterLiteral ex)
    {
        out.print("\"");
        out.print(ex.getEscapedValue());
        out.print("\".code");
    }

    private void printCastExpression(HaxeWriter out, CastExpression ex)
    {
        out.print("cast(");
        printExpression(out, ex.getExpression());
        out.print(", ");
        printTypeAccess(out, ex.getType());
        out.print(")");
    }

    private void printConditionalExpression(HaxeWriter out,
            ConditionalExpression ex)
    {
        printExpression(out, ex.getExpression());
        out.print(" ? ");
        printExpression(out, ex.getThenExpression());
        out.print(" : ");
        printExpression(out, ex.getElseExpression());
    }

    private void printAssignment(HaxeWriter out, Assignment ex)
    {
        printExpression(out, ex.getLeftHandSide());

        switch (ex.getOperator())
        {
            case ASSIGN:
                out.print(" = ");
                printExpression(out, ex.getRightHandSide());
                break;
            // haxe does not support shorthand-assignments, we need to duplicate
            // the lefhandside
            case BIT_AND_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") & ");
                printExpression(out, ex.getRightHandSide());
                break;
            case BIT_OR_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") | ");
                printExpression(out, ex.getRightHandSide());
                break;
            case BIT_XOR_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") ^ ");
                printExpression(out, ex.getRightHandSide());
                break;
            case DIVIDE_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") / ");
                printExpression(out, ex.getRightHandSide());
                break;
            case LEFT_SHIFT_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") << ");
                printExpression(out, ex.getRightHandSide());
                break;
            case MINUS_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") - ");
                printExpression(out, ex.getRightHandSide());
                break;
            case PLUS_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") + ");
                printExpression(out, ex.getRightHandSide());
                break;
            case REMAINDER_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") % ");
                printExpression(out, ex.getRightHandSide());
                break;
            case RIGHT_SHIFT_SIGNED_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") >> ");
                printExpression(out, ex.getRightHandSide());
                break;
            case RIGHT_SHIFT_UNSIGNED_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") >>> ");
                printExpression(out, ex.getRightHandSide());
                break;
            case TIMES_ASSIGN:
                out.print(" = (");
                printExpression(out, ex.getLeftHandSide());
                out.print(") * ");
                printExpression(out, ex.getRightHandSide());
                break;
        }
    }

    private void printArrayCreation(HaxeWriter out, ArrayCreation ex)
    {
        // ArrayUtils.create2DArray(1,2);
        out.print("ArrayUtils.create");
        out.print(((ArrayType) ex.getType().getType()).getDimensions());
        out.print("DArray<");
        printTypeAccess(out,
                ((ArrayType) ex.getType().getType()).getElementType());
        out.print(">(");

        if (ex.getInitializer() == null)
        {
            out.print("null, ");
        }
        else
        {
            printExpression(out, ex.getInitializer());
        }

        for (int i = 0; i < ex.getDimensions().size(); i++)
        {
            if (i > 0)
            {
                out.print(", ");
            }
            printExpression(out, ex.getDimensions().get(i));
        }
        out.print(")");

    }

    private void printArrayAccess(HaxeWriter out, ArrayAccess ex)
    {
        out.print("[");
        printExpression(out, ex.getIndex());
        out.print("]");
    }

    private void printClassInstanceCreation(HaxeWriter out,
            ClassInstanceCreation ex)
    {
        out.print("new ");

        printTypeAccess(out, ex.getType());
        out.print("(");

        for (int i = 0; i < ex.getArguments().size(); i++)
        {
            if (i > 0)
            {
                out.print(", ");
            }
            printExpression(out, ex.getArguments().get(i));
        }

        out.print(")");
    }

    private void printBooleanLiteral(HaxeWriter out, BooleanLiteral ex)
    {
        out.print(ex.isValue() ? "true" : "false");
    }

    private void printArrayLengthAccess(HaxeWriter out, ArrayLengthAccess ex)
    {
        out.print(".length");
    }

    private void printArrayInitializer(HaxeWriter out, ArrayInitializer ex)
    {
        out.print("[");

        for (int i = 0; i < ex.getExpressions().size(); i++)
        {
            if (i > 0)
            {
                out.print(", ");
            }
            printExpression(out, ex.getExpressions().get(i));
        }

        out.print("]");
    }

    private void printAnnotation(HaxeWriter out, Annotation ex)
    {
        // no annotation generation
    }

}
