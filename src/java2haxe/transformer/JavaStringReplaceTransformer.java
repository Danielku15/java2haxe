package java2haxe.transformer;

import java2haxe.IModelTransformer;
import java2haxe.MoDiscoXmiToHaxeProcessor;
import java2haxe.ModelUtils;

import org.eclipse.gmt.modisco.java.ImportDeclaration;
import org.eclipse.gmt.modisco.java.Model;
import org.eclipse.gmt.modisco.java.PrimitiveType;
import org.eclipse.gmt.modisco.java.Type;
import org.eclipse.gmt.modisco.java.TypeAccess;
import org.eclipse.gmt.modisco.java.emf.JavaFactory;

/**
 * this transformer replaces the java.lang.String by a primitive type string
 */
public class JavaStringReplaceTransformer implements IModelTransformer
{
    /**
     * Replaces the ocurrences of java.lang.String by a new primitive type
     * String.
     * 
     * @param model
     */
    @Override
    public void transform(MoDiscoXmiToHaxeProcessor procesor, Model model)
    {
        Type t = ModelUtils.findType(model, "java.lang.String");

        if (t == null)
        {
            procesor.warn(null, "could not find java.lang.String", false);
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
}
