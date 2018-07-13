package de.sub.goobi.helper;
/**
 * This file is part of the Goobi Application - a Workflow tool for the support of mass digitization.
 * 
 * Visit the websites for more information.
 *          - http://www.goobi.org
 *          - http://launchpad.net/goobi-production
 *          - http://gdz.sub.uni-goettingen.de
 *          - http://www.intranda.com
 *          - http://digiverso.com
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program; if not, write to the Free Software Foundation, Inc., 59
 * Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Linking this library statically or dynamically with other modules is making a combined work based on this library. Thus, the terms and conditions
 * of the GNU General Public License cover the whole combination. As a special exception, the copyright holders of this library give you permission to
 * link this library with independent modules to produce an executable, regardless of the license terms of these independent modules, and to copy and
 * distribute the resulting executable under terms of your choice, provided that you also meet, for each linked independent module, the terms and
 * conditions of the license of that module. An independent module is a module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but you are not obliged to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 *  A file visitor to perform a deep copy of a directory tree, copying the entire content of a source directory to the given targetDir
 *  <p/>
 *  usage:</br>{@code Files.walkFileTree(source directory, new DeepCopyFileVisitor(target directory));}
 * 
 * @author Florian Alpers
 *
 */
public class DeepCopyFileVisitor extends SimpleFileVisitor<Path>{

    private Path sourceDir = null;
    private final Path targetDir;
    
    public DeepCopyFileVisitor(Path targetDir) {
        this.targetDir = targetDir;
    }
    
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if(this.sourceDir == null) {  
            //visit base dir
            this.sourceDir = dir;
        } else {
            //visit sub directories
            Path currentTarget = this.sourceDir.relativize(dir);
            currentTarget = this.targetDir.resolve(currentTarget);
            //create the target sub directory
            Files.createDirectory(currentTarget);
        }
        return FileVisitResult.CONTINUE;
        
    }
    
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path currentTarget = this.sourceDir.relativize(file);
        currentTarget = this.targetDir.resolve(currentTarget);
        Files.copy(file, currentTarget);
        return FileVisitResult.CONTINUE;
    }
}
