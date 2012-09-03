package java2haxe;

import org.eclipse.gmt.modisco.java.AbstractTypeDeclaration;
import org.eclipse.gmt.modisco.java.Model;
import org.eclipse.gmt.modisco.java.Package;
import org.eclipse.gmt.modisco.java.Type;

/**
 * This class provides utility methods to work with a {@link Model}.
 */
public class ModelUtils
{
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
    public static Type findType(Model model, String fqn)
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
}
