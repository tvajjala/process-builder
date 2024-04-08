package com.codergists.log.appender;

import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.rolling.DefaultRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.net.Advertiser;

import java.io.Serializable;
import java.util.zip.Deflater;

@Plugin(name = ConcurrentLogAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class ConcurrentLogAppender extends AbstractOutputStreamAppender<RollingFileManager> {

    public static final String PLUGIN_NAME = "ConcurrentLogAppender";

    private static final int DEFAULT_BUFFER_SIZE = 8192;

    private final String fileName;
    private final String filePattern;

    private ConcurrentLogAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter,
                                  final RollingFileManager manager, final String fileName, final String filePattern,
                                  final boolean ignoreExceptions, final boolean immediateFlush, final Advertiser advertiser,
                                  final Property[] properties) {
        super(name, layout, filter, ignoreExceptions, immediateFlush, properties, manager);
        this.fileName = fileName;
        this.filePattern = filePattern;
    }

    public String getFileName() {
        return fileName;
    }


    public String getFilePattern() {
        return filePattern;
    }


    @PluginBuilderFactory
    public static <B extends ConcurrentLogAppender.Builder<B>> B newBuilder() {
        return new ConcurrentLogAppender.Builder<B>().asBuilder();
    }


    public static class Builder<B extends ConcurrentLogAppender.Builder<B>> extends AbstractOutputStreamAppender.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<ConcurrentLogAppender> {

        @PluginBuilderAttribute
        private String fileName;

        @PluginBuilderAttribute
        @Required
        private String filePattern;

        @PluginBuilderAttribute
        private boolean append = true;

        @PluginBuilderAttribute
        private boolean locking;

        @PluginElement("Policy")
        @Required
        private TriggeringPolicy policy;

        @PluginElement("Strategy")
        private RolloverStrategy strategy;

        @PluginBuilderAttribute
        private boolean advertise;

        @PluginBuilderAttribute
        private String advertiseUri;

        @PluginBuilderAttribute
        private boolean createOnDemand;

        @PluginBuilderAttribute
        private String filePermissions;

        @PluginBuilderAttribute
        private String fileOwner;

        @PluginBuilderAttribute
        private String fileGroup;


        @Override
        public ConcurrentLogAppender build() {
            if (!isValid()) {
                return null;
            }
            strategy = DefaultRolloverStrategy.newBuilder()
                    .withCompressionLevelStr(String.valueOf(Deflater.DEFAULT_COMPRESSION))
                    .withConfig(getConfiguration())
                    .build();
            final Layout<? extends Serializable> layout = getOrCreateLayout();
            final RollingFileManager manager = RollingFileManager.getFileManager(fileName, filePattern, append,
                    false, policy, strategy, advertiseUri, layout, 0, isImmediateFlush(),
                    createOnDemand, filePermissions, fileOwner, fileGroup, getConfiguration());

            manager.initialize();
            
            return new ConcurrentLogAppender(getName(), layout, getFilter(), manager, fileName, filePattern,
                    isIgnoreExceptions(), true,
                    null, getPropertyArray());
        }


    }

}
