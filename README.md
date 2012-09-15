# About
This program converts Java source to Haxe source using a modified
version of Sharpen, a Java to C# converter. 

# Contents
## sharpen.core
This eclipse project contains a modified version of Sharpen. 

## sharpen.jgit
This directory contains an example usage of Sharpen to convert Java to Haxe source. 
As an example it converts the JGit source to Haxe. The build file is based on the 
make file of NGit. 

# Usage
1. Import the Eclipse projects to your workspace
2. Export the sharpen.core project (Deployable plug-ins and fragments) to your Eclipse home directory 
3. Adjust the paths in the sharpen.jgit/build.xml to match your system.
4. Run the build.xml using ant (pull target)
5. The Haxe source can be found in the generated directory. 

or

1. Copy the dist/sharpen.core.jar to your Eclipse\Plugins directory
2. Adjust the paths in the sharpen.jgit/build.xml to match your system.
3. Run the build.xml using ant (pull target)
4. The Haxe source can be found in the generated directory. 

# Links
* Sharpen - http://community.versant.com/Documentation/Reference/db4o-7.12/java/reference/html/Content/sharpen.html
* JGit - http://www.eclipse.org/jgit/
* NGit - https://github.com/mono/ngit
