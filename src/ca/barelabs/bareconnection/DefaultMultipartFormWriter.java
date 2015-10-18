package ca.barelabs.bareconnection;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URLConnection;
import java.util.List;

import ca.barelabs.bareconnection.Entity.BinaryFileEntity;
import ca.barelabs.bareconnection.Entity.FileEntity;
import ca.barelabs.bareconnection.Entity.StringEntity;
import ca.barelabs.bareconnection.RestConnection.MultipartFormWriter;

public class DefaultMultipartFormWriter implements MultipartFormWriter {
    
    public static final String CRLF = "\r\n";
    
    
    private final List<Entity> mEntities;
    
    public DefaultMultipartFormWriter(List<Entity> entities) {
        mEntities = entities;
    }

    
    @Override
    public void onWrite(OutputStream out, String charset, String boundary) throws IOException {
        if(mEntities != null) {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, charset), true);
            for(Entity entity : mEntities) {
                if(entity instanceof StringEntity) {
                    StringEntity stringEntity = (StringEntity) entity;
                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"" + stringEntity.getName() + "\"").append(CRLF);
                    writer.append("Content-Type: text/plain; charset=" + stringEntity.getCharset()).append(CRLF);
                    writer.append(CRLF).append(stringEntity.getValue()).append(CRLF).flush();
                }
                else if(entity instanceof FileEntity) {
                    FileEntity fileEntity = (FileEntity) entity;
                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"textFile\"; filename=\"" + fileEntity.getName() + "\"").append(CRLF);
                    writer.append("Content-Type: text/plain; charset=" + fileEntity.getCharset()).append(CRLF); // Text file itself must be saved in this charset!
                    writer.append(CRLF).flush();
                    RestUtils.copy(new FileInputStream(fileEntity.getFile()), out);
                    out.flush(); // Important before continuing with writer!
                    writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
                }
                else if(entity instanceof BinaryFileEntity) {
                    BinaryFileEntity fileEntity = (BinaryFileEntity) entity;
                    writer.append("--" + boundary).append(CRLF);
                    writer.append("Content-Disposition: form-data; name=\"binaryFile\"; filename=\"" + fileEntity.getName() + "\"").append(CRLF);
                    writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(fileEntity.getFile().getName())).append(CRLF);
                    writer.append("Content-Transfer-Encoding: binary").append(CRLF);
                    writer.append(CRLF).flush();
                    RestUtils.copy(new FileInputStream(fileEntity.getFile()), out);
                    out.flush(); // Important before continuing with writer!
                    writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
                }
            }
            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF);
        }
    }
}
