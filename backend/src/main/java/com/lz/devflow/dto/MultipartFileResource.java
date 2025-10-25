package com.lz.devflow.dto;

import org.springframework.core.io.InputStreamResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MultipartFileResource extends InputStreamResource {

    private final String filename;
    private final long size;
    private String contentType;
    private byte[] fileContent;
    private String fileId;

    public MultipartFileResource(byte[] file, String filename, long size, String fileId) {
        super(new ByteArrayInputStream(file));
        this.filename = filename;
        this.size = size;
        this.contentType = "image/png";
        this.fileContent = file;
        this.fileId = fileId;
    }

    public MultipartFileResource(InputStream inputStream, String filename, long size) {
        super(inputStream);
        this.filename = filename;
        this.size = size;
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public long contentLength() throws IOException {
        return this.size;
    }

    public long getSize() {
        return size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public byte[] getFileContent() {
        return fileContent;
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }
}
