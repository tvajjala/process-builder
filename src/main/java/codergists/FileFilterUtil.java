package codergists;

import org.apache.commons.io.filefilter.AgeFileFilter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Stream;

import static java.time.LocalDate.now;
import static java.time.ZoneId.systemDefault;
import static java.util.Date.from;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.apache.commons.io.FileUtils.listFilesAndDirs;

/**
 * Utility class cleanup archived session files which are older than 2 weeks
 *
 * @author tvajjala
 */
public class FileFilterUtil {

  /**
   * Threshold time
   */
  private static final int THRESHOLD = 2;//weeks
  /**
   * cutOff date
   */
  private static final Date cutoffDate = from(now().minusWeeks(THRESHOLD).atStartOfDay(systemDefault()).toInstant());

  /**
   * ThreadShould date filter
   */
  private static AgeFileFilter ageFileFilter = new AgeFileFilter(cutoffDate);

  /**
   * Directories to search for older files
   */
  private static String[] DEFAULT_DIRS = new String[]{"/scratch/.mv-creator", "/scratch/logs"};

  /**
   * Scan Directory
   *
   * @param parentDir
   */
  static void scanDirectory(File parentDir) {

    System.out.println("Searching old files under" + parentDir + "with cutoff date" + cutoffDate);

    Collection<File> list = listFilesAndDirs(parentDir, ageFileFilter, ageFileFilter);

    list.stream().filter(f -> !f.isDirectory())
        .forEach(f -> System.out.println(f + " deleted ?" + f.delete()));

    list.stream().filter(dir -> dir.isDirectory() && !dir.equals(parentDir))
        .forEach(dir -> {
          System.out.println("Deleting dir " + dir);
          try {
            if (dir.list().length != 0) {
              System.out.println(dir + " directory not empty");
              return;
            }
            deleteDirectory(dir);
          } catch (IOException ioException) {
            System.out.println("Failed to delete directory" + dir);
          }
        });
  }

  static void cleanupOldFiles(String... parentDirs) {
    try {
      Stream.of(parentDirs)
            .map(dir -> new File(dir))
            .forEach(parentDir -> scanDirectory(parentDir));
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  /**
   * Public API Specify default parent directories
   */
  public static void deleteOldFiles() {
    cleanupOldFiles(DEFAULT_DIRS);
  }

  /**
   * Specify command line arguments (standalone mode)
   *
   * @param args list of directories
   */
  public static void main(String[] args) {
    cleanupOldFiles(args);
  }

}
