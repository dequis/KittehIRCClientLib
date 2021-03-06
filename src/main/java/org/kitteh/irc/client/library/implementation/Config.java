/*
 * * Copyright (C) 2013-2015 Matt Baxter http://kitteh.org
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS
 * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.kitteh.irc.client.library.implementation;

import org.kitteh.irc.client.library.ClientBuilder;
import org.kitteh.irc.client.library.util.ToStringer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.net.ssl.TrustManagerFactory;
import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Stores a Client's configured data from the {@link ClientBuilder}.
 * <p>
 * This class is for internal use only.
 */
final class Config {
    /**
     * Represents a configuration entry.
     *
     * @param <Type>
     */
    static final class Entry<Type> {
        private final Type defaultValue;
        private final Class<Type> type;

        private Entry(@Nullable Type defaultValue, @Nonnull Class<Type> type) {
            this.defaultValue = defaultValue;
            this.type = type;
        }

        /**
         * Gets the entry's default value.
         *
         * @return the default value
         */
        @Nullable
        private Type getDefault() {
            return this.defaultValue;
        }

        /**
         * Gets the type of the entry.
         *
         * @return the entry type
         */
        @Nonnull
        private Class<Type> getType() {
            return this.type;
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).toString();
        }
    }

    abstract static class Wrapper<Type> {
        private final Consumer<Type> consumer;

        Wrapper(@Nonnull Consumer<Type> consumer) {
            this.consumer = consumer;
        }

        @Nonnull
        Consumer<Type> getConsumer() {
            return this.consumer;
        }
    }

    static final class ExceptionConsumerWrapper extends Wrapper<Exception> {
        ExceptionConsumerWrapper(@Nonnull Consumer<Exception> consumer) {
            super(consumer);
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).toString();
        }
    }

    static final class StringConsumerWrapper extends Wrapper<String> {
        StringConsumerWrapper(@Nonnull Consumer<String> consumer) {
            super(consumer);
        }

        @Nonnull
        @Override
        public String toString() {
            return new ToStringer(this).toString();
        }
    }

    static final Entry<String> NAME = new Entry<>("Unnamed", String.class);
    static final Entry<InetSocketAddress> BIND_ADDRESS = new Entry<>(null, InetSocketAddress.class);
    static final Entry<ExceptionConsumerWrapper> LISTENER_EXCEPTION = new Entry<>(null, ExceptionConsumerWrapper.class);
    static final Entry<StringConsumerWrapper> LISTENER_INPUT = new Entry<>(null, StringConsumerWrapper.class);
    static final Entry<StringConsumerWrapper> LISTENER_OUTPUT = new Entry<>(null, StringConsumerWrapper.class);
    static final Entry<Integer> MESSAGE_DELAY = new Entry<>(1200, Integer.class);
    static final Entry<String> NICK = new Entry<>("Kitteh", String.class);
    static final Entry<String> REAL_NAME = new Entry<>("Kitteh", String.class);
    static final Entry<InetSocketAddress> SERVER_ADDRESS = new Entry<>(new InetSocketAddress("localhost", 6697), InetSocketAddress.class);
    static final Entry<String> SERVER_PASSWORD = new Entry<>(null, String.class);
    static final Entry<Boolean> SSL = new Entry<>(true, Boolean.class);
    static final Entry<File> SSL_KEY_CERT_CHAIN = new Entry<>(null, File.class);
    static final Entry<File> SSL_KEY = new Entry<>(null, File.class);
    static final Entry<String> SSL_KEY_PASSWORD = new Entry<>(null, String.class);
    static final Entry<TrustManagerFactory> SSL_TRUST_MANAGER_FACTORY = new Entry<>(null, TrustManagerFactory.class);
    static final Entry<String> USER = new Entry<>("Kitteh", String.class);
    static final Entry<String> WEBIRC_HOST = new Entry<>(null, String.class);
    static final Entry<InetAddress> WEBIRC_IP = new Entry<>(null, InetAddress.class);
    static final Entry<String> WEBIRC_PASSWORD = new Entry<>(null, String.class);
    static final Entry<String> WEBIRC_USER = new Entry<>(null, String.class);

    /**
     * Magical null value for {@link java.util.concurrent.ConcurrentHashMap}.
     * Must be static because this value is shared across cloned Configs.
     */
    private static final Object NULL = new Object();

    private final Map<Entry<?>, Object> map = new ConcurrentHashMap<>();

    @Nonnull
    @Override
    protected Config clone() {
        Config config = new Config();
        config.map.putAll(this.map);
        return config;
    }

    /**
     * Gets a stored configuration entry.
     *
     * @param entry entry to acquire
     * @param <Type> entry type
     * @return the stored entry, or the default value if not set
     */
    @Nullable
    <Type> Type get(@Nonnull Entry<Type> entry) {
        if (this.map.containsKey(entry)) {
            Object value = this.map.get(entry);
            if ((value != NULL) && entry.getType().isAssignableFrom(value.getClass())) {
                @SuppressWarnings("unchecked")
                Type castValue = (Type) value;
                return castValue;
            }
            return null;
        }
        return entry.getDefault();
    }

    /**
     * Gets a stored configuration entry that is never null.
     *
     * @param entry entry to acquire
     * @param <Type> entry type
     * @return the stored entry
     * @throws NullPointerException for a null entry
     */
    @Nonnull
    <Type> Type getNotNull(@Nonnull Entry<Type> entry) {
        Type t = this.get(entry);
        if (t == null) {
            throw new NullPointerException();
        }
        return t;
    }

    /**
     * Sets a configuration entry.
     *
     * @param entry entry to set
     * @param value value to set for the given entry
     * @param <Type> entry type
     */
    <Type> void set(@Nonnull Entry<Type> entry, @Nullable Type value) {
        this.map.put(entry, (value != null) ? value : NULL);
    }

    @Nonnull
    @Override
    public String toString() {
        return new ToStringer(this).toString();
    }
}
