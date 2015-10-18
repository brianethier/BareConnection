package ca.barelabs.bareconnection;

import java.io.File;


public abstract class Entity {

    protected final String mName;
    protected final String mCharset;
    
    private Entity(String name) {
        this(name, RestConnection.DEFAULT_CHARSET);
    }
    
    private Entity(String name, String charset) {
        mName = name;
        mCharset = charset;
    }
    
    public String getName() {
        return mName;
    }
    
    public String getCharset() {
        return mCharset;
    }
    

    
    public static final class StringEntity extends Entity {
        
        private final String mValue;
        
        public StringEntity(String name, String value) {
            this(name, value, RestConnection.DEFAULT_CHARSET);
        }
        
        public StringEntity(String name, String value, String charset) {
            super(name, charset);
            mValue = value;
        }
        
        public String getValue() {
            return mValue;
        }
    }
    
    
    public static final class FileEntity extends Entity {
        
        private final File mFile;
        
        public FileEntity(String name, File file) {
            this(name, file, RestConnection.DEFAULT_CHARSET);
        }
        
        public FileEntity(String name, File file, String charset) {
            super(name, charset);
            mFile = file;
        }
        
        public File getFile() {
            return mFile;
        }
    }
    
    
    public static final class BinaryFileEntity extends Entity {
        
        private final File mFile;
        
        public BinaryFileEntity(String name, File file) {
            this(name, file, RestConnection.DEFAULT_CHARSET);
        }
        
        public BinaryFileEntity(String name, File file, String charset) {
            super(name, charset);
            mFile = file;
        }
        
        public File getFile() {
            return mFile;
        }
    }
}
