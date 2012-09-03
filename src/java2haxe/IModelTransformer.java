package java2haxe;

import org.eclipse.gmt.modisco.java.Model;

/**
 * The classes implementing this interface are responsible for transforming the
 * java model into a haxe compatible model. This task is required to
 * remove/change elments which are not supported by haxe.
 */
public interface IModelTransformer
{
    public void transform(MoDiscoXmiToHaxeProcessor procesor, Model model);
}
