package ca.barelabs.bareconnection;


public interface BackOffPolicy {
    
    boolean isBackOffRequired(int statusCode);

    boolean backOff();
    
    void reset();

}
