package execjar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

public class ExecJarFactory {

    private File outputFile;
    private String mainClass;
    private List<File> dependencies = new ArrayList<>();

    public void addDependency(File file) {
        Objects.requireNonNull(file, "file");
        dependencies.add(file);
    }

    public void setMainClass(String mainClass) {
        this.mainClass = mainClass;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    private Set<String> entries = new HashSet<>();
    private Set<String> duplicateEntries = new HashSet<>();

    public Set<String> getEntries() {
        return entries;
    }

    public Set<String> getDuplicateEntries() {
        return duplicateEntries;
    }

    public void execute() throws IOException {
        outputFile.getParentFile().mkdirs();

        try (JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputFile))) {
            jarOutputStream.putNextEntry(new JarEntry("META-INF/MANIFEST.MF"));
            jarOutputStream.write(("Main-Class: " + mainClass + "\n").getBytes());
            entries.add("META-INF/MANIFEST.MF");

            for (File dependency : dependencies) {
                appendJarFile(jarOutputStream, entries, dependency);
            }
        }
    }

    private void appendJarFile(JarOutputStream jarOutputStream, Set<String> entries, File dependency) throws IOException {
        JarInputStream jarInputStream = new JarInputStream(new FileInputStream(dependency));
        ZipEntry jarEntry;
        while ((jarEntry = jarInputStream.getNextEntry()) != null) {
            if (jarEntry.getName().equals("module-info.class")) {
                continue;
            }
            if (entries.contains(jarEntry.getName())) {
                duplicateEntries.add(dependency + "!" + jarEntry.getName());
                continue;
            }

            jarOutputStream.putNextEntry(new JarEntry(jarEntry.getName()));
            transferTo(jarInputStream, jarOutputStream);
            entries.add(jarEntry.getName());
        }
    }

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private void transferTo(InputStream in, OutputStream out) throws IOException {
        Objects.requireNonNull(out, "out");
        long transferred = 0;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int read;
        while ((read = in.read(buffer, 0, DEFAULT_BUFFER_SIZE)) >= 0) {
            out.write(buffer, 0, read);
            transferred += read;
        }
    }
}
