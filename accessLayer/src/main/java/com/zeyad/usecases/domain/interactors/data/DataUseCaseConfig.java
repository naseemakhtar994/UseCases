package com.zeyad.usecases.domain.interactors.data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.zeyad.usecases.data.executor.JobExecutor;
import com.zeyad.usecases.data.mappers.DAOMapperUtil;
import com.zeyad.usecases.data.mappers.DefaultDAOMapper;
import com.zeyad.usecases.data.mappers.IDAOMapper;
import com.zeyad.usecases.data.mappers.IDAOMapperUtil;
import com.zeyad.usecases.domain.executors.PostExecutionThread;
import com.zeyad.usecases.domain.executors.ThreadExecutor;
import com.zeyad.usecases.domain.executors.UIThread;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

/**
 * @author by ZIaDo on 12/9/16.
 */

public class DataUseCaseConfig {

    private Context context;
    private IDAOMapperUtil entityMapper;
    private OkHttpClient.Builder okHttpBuilder;
    private Cache cache;
    private String baseUrl;
    private boolean withCache, withRealm;
    private int cacheSize;
    private ThreadExecutor threadExecutor;
    private PostExecutionThread postExecutionThread;

    private DataUseCaseConfig(Builder dataUseCaseConfigBuilder) {
        context = dataUseCaseConfigBuilder.getContext();
        entityMapper = dataUseCaseConfigBuilder.getEntityMapper();
        okHttpBuilder = dataUseCaseConfigBuilder.getOkHttpBuilder();
        cache = dataUseCaseConfigBuilder.getCache();
        baseUrl = dataUseCaseConfigBuilder.getBaseUrl();
        withCache = dataUseCaseConfigBuilder.isWithCache();
        withRealm = dataUseCaseConfigBuilder.isWithRealm();
        cacheSize = dataUseCaseConfigBuilder.getCacheSize();
        threadExecutor = dataUseCaseConfigBuilder.getThreadExecutor();
        postExecutionThread = dataUseCaseConfigBuilder.getPostExecutionThread();
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    IDAOMapperUtil getEntityMapper() {
        if (entityMapper == null) {
            return new DAOMapperUtil() {
                @NonNull
                @Override
                public IDAOMapper getDataMapper(Class dataClass) {
                    return new DefaultDAOMapper();
                }
            };
        }
        return entityMapper;
    }

    public ThreadExecutor getThreadExecutor() {
        return threadExecutor == null ? new JobExecutor() : threadExecutor;
    }

    public PostExecutionThread getPostExecutionThread() {
        return postExecutionThread == null ? new UIThread() : postExecutionThread;
    }

    OkHttpClient.Builder getOkHttpBuilder() {
        return okHttpBuilder;
    }

    public Cache getCache() {
        return cache;
    }

    String getBaseUrl() {
        return baseUrl != null ? baseUrl : "";
    }

    boolean isWithRealm() {
        return withRealm;
    }

    boolean isWithCache() {
        return withCache;
    }

    int getCacheSize() {
        return cacheSize == 0 ? 8192 : cacheSize;
    }

    public static class Builder {
        private Context context;
        private IDAOMapperUtil entityMapper;
        private OkHttpClient.Builder okHttpBuilder;
        private Cache cache;
        private String baseUrl;
        private boolean withCache, withRealm;
        private int cacheSize;
        private ThreadExecutor threadExecutor;
        private PostExecutionThread postExecutionThread;

        public Builder(Context context) {
            this.context = context;
        }

        @NonNull
        public Builder threadExecutor(ThreadExecutor threadExecutor) {
            this.threadExecutor = threadExecutor;
            return this;
        }

        @NonNull
        public Builder postExecutionThread(PostExecutionThread postExecutionThread) {
            this.postExecutionThread = postExecutionThread;
            return this;
        }

        @NonNull
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        @NonNull
        public Builder entityMapper(IDAOMapperUtil entityMapper) {
            this.entityMapper = entityMapper;
            return this;
        }

        @NonNull
        public Builder okHttpBuilder(OkHttpClient.Builder okHttpBuilder) {
            this.okHttpBuilder = okHttpBuilder;
            return this;
        }

        @NonNull
        public Builder okhttpCache(Cache cache) {
            this.cache = cache;
            return this;
        }

        @NonNull
        public Builder withRealm() {
            this.withRealm = true;
            return this;
        }

        @NonNull
        public Builder withCache() {
            this.withCache = true;
            return this;
        }

        @NonNull
        public Builder cacheSize(int cacheSize) {
            this.cacheSize = cacheSize;
            return this;
        }

        Context getContext() {
            return context;
        }

        public ThreadExecutor getThreadExecutor() {
            return threadExecutor;
        }

        public PostExecutionThread getPostExecutionThread() {
            return postExecutionThread;
        }

        IDAOMapperUtil getEntityMapper() {
            return entityMapper;
        }

        OkHttpClient.Builder getOkHttpBuilder() {
            return okHttpBuilder;
        }

        Cache getCache() {
            return cache;
        }

        void setCache(Cache cache) {
            this.cache = cache;
        }

        String getBaseUrl() {
            return baseUrl;
        }

        boolean isWithRealm() {
            return withRealm;
        }

        boolean isWithCache() {
            return withCache;
        }

        int getCacheSize() {
            return cacheSize;
        }

        @NonNull
        public DataUseCaseConfig build() {
            return new DataUseCaseConfig(this);
        }
    }
}
