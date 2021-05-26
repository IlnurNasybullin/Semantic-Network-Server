package app.server.util;

import app.server.resource.LanguageResource;

public class GenerationData {

    private String fileName;

    private LanguageResource from;
    private LanguageResource to;
    private FileFormat format;

    public LanguageResource getFrom() {
        return from;
    }

    public void setFrom(LanguageResource from) {
        this.from = from;
    }

    public LanguageResource getTo() {
        return to;
    }

    public void setTo(LanguageResource to) {
        this.to = to;
    }

    public FileFormat getFormat() {
        return format;
    }

    public void setFormat(FileFormat format) {
        this.format = format;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
}
