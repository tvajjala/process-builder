package org.example;

import java.nio.file.Files;
import java.nio.file.Paths;

import static java.nio.file.Files.isDirectory;

/**
 * java FileWalkDemo
 * /scratch/tmp.C4e4nec7Vh/volume_mount/backup/initial-data/casrepos/glcm
 * /scratch/tmp.C4e4nec7Vh/volume_mount/backup/post-repo-27126358144C27869DDFEADA2A11B0AF/casrepos/glcm
 */
public class FileWalkDemo {

  public static void main(String[] args) throws Exception {

    Files.walk(Paths.get("/scratch/tmp.C4e4nec7Vh/volume_mount/backup"), 2)
         .filter(path -> isDirectory(path.resolve("casrepos").resolve("glcm")))
         .map(path -> path.resolve("casrepos").resolve("glcm"))
         .forEach(a -> System.out.println(a));

  }
}
