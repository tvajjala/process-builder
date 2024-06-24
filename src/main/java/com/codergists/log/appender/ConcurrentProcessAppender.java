package com.codergists.log.appender;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.rolling.DirectFileRolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.RollingFileManager;
import org.apache.logging.log4j.core.appender.rolling.RolloverStrategy;
import org.apache.logging.log4j.core.appender.rolling.TriggeringPolicy;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Slf4j
@Plugin(name = ConcurrentProcessAppender.PLUGIN_NAME, category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class ConcurrentProcessAppender extends AbstractOutputStreamAppender<RollingFileManager> {

    public static final String PLUGIN_NAME = "ConcurrentProcessAppender";

    String fileName;
    String filePattern;

    private ConcurrentProcessAppender(final String name, final Layout<? extends Serializable> layout,
                                      Filter filter,
                                      final boolean ignoreExceptions,
                                      final boolean immediateFlush,
                                      final Property[] properties,
                                      final RollingFileManager manager, final String fileName, final String filePattern) {
        super(name, layout, filter, ignoreExceptions, immediateFlush, properties, manager);
        this.fileName = fileName;
        this.filePattern = filePattern;
    }

    @Override
    public boolean stop(final long timeout, final TimeUnit timeUnit) {
        setStopping();
        final boolean stopped = super.stop(timeout, timeUnit, false);
        setStopped();
        return stopped;
    }


    @Override
    public void append(final LogEvent event) {
        getManager().checkRollover(event);
        writeByteArrayToManager(event);
    }


    /**
     * Invoked by log4j to prepare Appender
     *
     * @param <B> Builder
     * @return builder
     */
    @PluginBuilderFactory
    public static <B extends ConcurrentProcessAppender.Builder<B>> B newBuilder() {
        return new Builder<B>().asBuilder();
    }


    public static class Builder<B extends ConcurrentProcessAppender.Builder<B>> extends AbstractOutputStreamAppender.Builder<B>
            implements org.apache.logging.log4j.core.util.Builder<ConcurrentProcessAppender> {

        @PluginElement("Policy")
        @Required
        private TriggeringPolicy policy;

        @PluginElement("Strategy")
        private RolloverStrategy strategy;
        @PluginBuilderAttribute
        private String fileName;

        @PluginBuilderAttribute
        @Required
        private String filePattern;

        @Override
        public ConcurrentProcessAppender build() {
            if (!isValid()) {
                throw new RuntimeException("Failed to prepare appender");
            }

            final boolean isBufferedIo = isBufferedIo();
            final int bufferSize = getBufferSize();

            if (strategy instanceof DirectFileRolloverStrategy) {
                throw new RuntimeException("DirectFileRolloverStrategy not supported");
            }


            final Layout<? extends Serializable> layout = getOrCreateLayout();

            //createOnDemand=True to rollover happen
            final RollingFileManager manager = RollingFileManager.getFileManager(fileName, filePattern, true,
                    isBufferedIo, policy, strategy, null, layout, bufferSize, isImmediateFlush(),
                    true, "rwxr-x---", "tvajjala", "staff", getConfiguration());

            manager.initialize();

            return new ConcurrentProcessAppender(getName(), layout, getFilter(),
                    false,
                    false,
                    getPropertyArray(),
                    manager, fileName, filePattern);
        }


    }

}
