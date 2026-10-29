package randy.framework.model;

import java.util.List;

/**
 * Fichier uploadé : nom d'origine, extension et contenu en octets.
 * Déclarer un paramètre RMapFile (ou Map<String, RMapFile>) suffit pour le recevoir.
 */
public class RMapFile {
    public String filename;
    public String extension;
    public List<Byte> file;

    public RMapFile(String filename, String extension, List<Byte> file) {
        this.filename = filename;
        this.extension = extension;
        this.file = file;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public List<Byte> getFile() {
        return file;
    }

    public void setFile(List<Byte> file) {
        this.file = file;
    }

    public Byte[] toArray() {
        Byte[] array = new Byte[file.size()];
        for (int i = 0; i < file.size(); i++) {
            array[i] = file.get(i);
        }
        return array;
    }

    @Override
    public String toString() {
        return "UploadedFile{filename='" + filename
                + "', extension='" + extension
                + "', size=" + (file != null ? file.size() : 0) + " bytes}";
    }
}