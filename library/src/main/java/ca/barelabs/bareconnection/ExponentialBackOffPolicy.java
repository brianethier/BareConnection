package ca.barelabs.bareconnection;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class ExponentialBackOffPolicy implements BackOffPolicy {
    
    public static final ExponentialBackOffPolicy getDefault() {
        return new ExponentialBackOffPolicy.Builder()
            .retryStatusCode(RestConnection.SC_UNAVAILABLE)
            .build();
    }
    
    public static final double DEFAULT_MULTIPLIER = 1.5;
    public static final long DEFAULT_INITIAL_SLEEP = 500;
    public static final long DEFAULT_MAX_SLEEP = 30000;

    private final Random mRandom = new Random();
    private final List<Integer> mRetryStatusCodes;
    private final double mMultiplier;
    private final long mInitialSleep;
    private final long mMaxSleep;
    private long mSleep;
       
    
    public ExponentialBackOffPolicy(List<Integer> retryStatusCodes, double multiplier, long initialSleep, long maxSleep) {
        mRetryStatusCodes = retryStatusCodes;
        mMultiplier = multiplier < 1 ? 1 : multiplier;
        mInitialSleep = initialSleep < 0 ? 0 : initialSleep;
        mMaxSleep = maxSleep < initialSleep ? initialSleep : maxSleep;
        reset();
    }

    @Override
    public boolean isBackOffRequired(int statusCode) {
        return mRetryStatusCodes != null && mRetryStatusCodes.contains(statusCode);
    }

    @Override
    public boolean backOff() {
        try {
            if (mSleep < mMaxSleep) {
                Thread.sleep(getSleepAndIncrement());
                return true;
            }
        } catch (InterruptedException e) {
        }
        return false;
    }

    @Override
    public void reset() {
        mSleep = mInitialSleep;
    }
    
    private long getSleepAndIncrement() {
        try {
            return mSleep;
        } finally {
            mSleep = (long) ((mSleep * mMultiplier) + (mRandom.nextInt(200) - 100)); // random offset +/- milliseconds
        }
    }
        
    
    public static final class Builder {

        private List<Integer> mRetryStatusCodes = new ArrayList<>();
        private double mMultiplier = DEFAULT_MULTIPLIER;
        private long mInitialSleep = DEFAULT_INITIAL_SLEEP;
        private long mMaxSleep = DEFAULT_MAX_SLEEP;
        
        public Builder retryStatusCode(int statusCode) {
            if (!mRetryStatusCodes.contains(statusCode)) {
                mRetryStatusCodes.add(statusCode);
            }
            return this;
        }
        
        public Builder multiplier(double multiplier) {
            mMultiplier = multiplier;
            return this;
        }
        
        public Builder initialSleep(long initialSleep) {
            mInitialSleep = initialSleep;
            return this;
        }
        
        public Builder maxSleep(long maxSleep) {
            mMaxSleep = maxSleep;
            return this;
        }
        
        public ExponentialBackOffPolicy build() {
            return new ExponentialBackOffPolicy(mRetryStatusCodes, mMultiplier, mInitialSleep, mMaxSleep);
        }
    }
}
