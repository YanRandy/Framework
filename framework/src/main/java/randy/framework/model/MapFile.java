package randy.framework.model;

import java.util.List;

public class MapFile {
    public String filename;
    public String extension;
    public List<Byte> file;

    public MapFile(String filename, String extension, List<Byte> file) {
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