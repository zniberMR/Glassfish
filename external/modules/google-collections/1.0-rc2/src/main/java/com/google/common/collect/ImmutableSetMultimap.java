/*
 * Copyright (C) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nullable;

/**
 * An immutable {@link SetMultimap} with reliable user-specified key and value
 * iteration order. Does not permit null keys or values.
 *
 * <p>Unlike {@link Multimaps#unmodifiableSetMultimap(SetMultimap)}, which is
 * a <i>view</i> of a separate multimap which can still change, an instance of
 * {@code ImmutableSetMultimap} contains its own data and will <i>never</i>
 * change. {@code ImmutableSetMultimap} is convenient for
 * {@code public static final} multimaps ("constant multimaps") and also lets
 * you easily make a "defensive copy" of a multimap provided to your class by
 * a caller.
 *
 * <p><b>Note</b>: Although this class is not final, it cannot be subclassed as
 * it has no public or protected constructors. Thus, instances of this class
 * are guaranteed to be immutable.
 *
 * @author Mike Ward
 */
@GwtCompatible
public class ImmutableSetMultimap<K, V>
    extends ImmutableMultimap<K, V>
    implements SetMultimap<K, V> {

  private static final ImmutableSetMultimap<Object, Object> EMPTY_MULTIMAP
      = new EmptyMultimap();

  private static class EmptyMultimap
      extends ImmutableSetMultimap<Object, Object> {
    EmptyMultimap() {
      super(ImmutableMap.<Object, ImmutableSet<Object>>of(), 0);
    }
    Object readResolve() {
      return EMPTY_MULTIMAP; // preserve singleton property
    }
    private static final long serialVersionUID = 0;
  }

  /** Returns the empty multimap. */
  // Casting is safe because the multimap will never hold any elements.
  @SuppressWarnings("unchecked")
  public static <K, V> ImmutableSetMultimap<K, V> of() {
    return (ImmutableSetMultimap<K, V>) EMPTY_MULTIMAP;
  }

  /**
   * Returns an immutable multimap containing a single entry.
   */
  public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1) {
    ImmutableSetMultimap.Builder<K, V> builder = ImmutableSetMultimap.builder();
    builder.put(k1, v1);
    return builder.build();
  }

  /**
   * Returns an immutable multimap containing the given entries, in order.
   *
   * @throws IllegalArgumentException if duplicate key-value pairs are provided
   */
  public static <K, V> ImmutableSetMultimap<K, V> of(K k1, V v1, K k2, V v2) {
    ImmutableSetMultimap.Builder<K, V> builder = ImmutableSetMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    return builder.build();
  }

  /**
   * Returns an immutable multimap containing the given entries, in order.
   *
   * @throws IllegalArgumentException if duplicate key-value pairs are provided
   */
  public static <K, V> ImmutableSetMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3) {
    ImmutableSetMultimap.Builder<K, V> builder = ImmutableSetMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    builder.put(k3, v3);
    return builder.build();
  }

  /**
   * Returns an immutable multimap containing the given entries, in order.
   *
   * @throws IllegalArgumentException if duplicate key-value pairs are provided
   */
  public static <K, V> ImmutableSetMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
    ImmutableSetMultimap.Builder<K, V> builder = ImmutableSetMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    builder.put(k3, v3);
    builder.put(k4, v4);
    return builder.build();
  }

  /**
   * Returns an immutable multimap containing the given entries, in order.
   *
   * @throws IllegalArgumentException if duplicate key-value pairs are provided
   */
  public static <K, V> ImmutableSetMultimap<K, V> of(
      K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
    ImmutableSetMultimap.Builder<K, V> builder = ImmutableSetMultimap.builder();
    builder.put(k1, v1);
    builder.put(k2, v2);
    builder.put(k3, v3);
    builder.put(k4, v4);
    builder.put(k5, v5);
    return builder.build();
  }

  // looking for of() with > 5 entries? Use the builder instead.

  /**
   * Returns a new {@link Builder}.
   */
  public static <K, V> Builder<K, V> builder() {
    return new Builder<K, V>();
  }

  /**
   * Multimap for {@link ImmutableSetMultimap.Builder} that maintains key
   * and value orderings, disallows duplicate values, and performs better than
   * {@link LinkedHashMultimap}.
   */
  private static class BuilderMultimap<K, V> extends StandardMultimap<K, V> {
    BuilderMultimap() {
      super(new LinkedHashMap<K, Collection<V>>());
    }
    @Override Collection<V> createCollection() {
      return Sets.newLinkedHashSet();
    }
    private static final long serialVersionUID = 0;
  }

  public static final class Builder<K, V>
      extends ImmutableMultimap.Builder<K, V> {
    private static final String ADDED_TWICE
        = "Key-value pair {%s, %s} was added twice";

    private final Multimap<K, V> builderMultimap = new BuilderMultimap<K, V>();

    /**
     * Creates a new builder. The returned builder is equivalent to the builder
     * generated by {@link ImmutableSetMultimap#builder}.
     */
    public Builder() {}

    /**
     * Adds a key-value mapping to the built multimap.
     *
     * @throws IllegalArgumentException if the key-value pair was previously
     *      added
     */
    @Override public Builder<K, V> put(K key, V value) {
      checkArgument(
          builderMultimap.put(checkNotNull(key), checkNotNull(value)),
          ADDED_TWICE, key, value);
      return this;
    }

    /**
     * Stores a collection of values with the same key in the built multimap.
     *
     * @throws NullPointerException if {@code key}, {@code values}, or any
     *     element in {@code values} is null. The builder is left in an invalid
     *     state.
     * @throws IllegalArgumentException if {@code values} contains any
     *     duplicates or if the one of the key-value pairs was previously added
     */
    @Override public Builder<K, V> putAll(K key, Iterable<? extends V> values) {
      Collection<V> collection = builderMultimap.get(checkNotNull(key));
      for (V value : values) {
        checkArgument(collection.add(checkNotNull(value)),
            ADDED_TWICE, key, value);
      }
      return this;
    }

    /**
     * Stores an array of values with the same key in the built multimap.
     *
     * @throws NullPointerException if the key or any value is null. The
     *     builder is left in an invalid state.
     * @throws IllegalArgumentException if {@code values} contains any
     *     duplicates or if the one of the key-value pairs was previously added
     */
    @Override public Builder<K, V> putAll(K key, V... values) {
      return putAll(key, Arrays.asList(values));
    }

    /**
     * Stores another multimap's entries in the built multimap. The generated
     * multimap's key and value orderings correspond to the iteration ordering
     * of the {@code multimap.asMap()} view, with new keys and values following
     * any existing keys and values.
     *
     * @throws NullPointerException if any key or value in {@code multimap} is
     *     null. The builder is left in an invalid state.
     * @throws IllegalArgumentException if {@code multimap} contains any
     *     duplicate key-value pairs or if one of the key-value pairs was
     *     previously added
     */
    @Override public Builder<K, V> putAll(
        Multimap<? extends K, ? extends V> multimap) {
      for (Map.Entry<? extends K, ? extends Collection<? extends V>> entry
          : multimap.asMap().entrySet()) {
        putAll(entry.getKey(), entry.getValue());
      }
      return this;
    }

    /**
     * Returns a newly-created immutable set multimap.
     */
    public ImmutableSetMultimap<K, V> build() {
      return copyOf(builderMultimap);
    }
  }

  /**
   * Returns an immutable set multimap containing the same mappings as
   * {@code multimap}. The generated multimap's key and value orderings
   * correspond to the iteration ordering of the {@code multimap.asMap()} view.
   *
   * <p><b>Note:</b> Despite what the method name suggests, if
   * {@code multimap} is an {@code ImmutableSetMultimap}, no copy will actually
   * be performed, and the given multimap itself will be returned.
   *
   * @throws NullPointerException if any key or value in {@code multimap} is
   *     null
   * @throws IllegalArgumentException if {@code multimap} contains any duplicate
   *     key-value pairs
   */
  public static <K, V> ImmutableSetMultimap<K, V> copyOf(
      Multimap<? extends K, ? extends V> multimap) {
    if (multimap.isEmpty()) {
      return of();
    }

    if (multimap instanceof ImmutableSetMultimap) {
      @SuppressWarnings("unchecked") // safe since multimap is not writable
      ImmutableSetMultimap<K, V> kvMultimap
          = (ImmutableSetMultimap<K, V>) multimap;
      return kvMultimap;
    }

    ImmutableMap.Builder<K, ImmutableSet<V>> builder = ImmutableMap.builder();
    int size = 0;

    for (Map.Entry<? extends K, ? extends Collection<? extends V>> entry
        : multimap.asMap().entrySet()) {
      K key = entry.getKey();
      Collection<? extends V> values = entry.getValue();
      ImmutableSet<V> set = ImmutableSet.copyOf(values);
      checkArgument(set.size() == values.size(),
          "Duplicate values exist for key %s among %s", key, values);
      if (!set.isEmpty()) {
        builder.put(key, set);
        size += set.size();
      }
    }

    return new ImmutableSetMultimap<K, V>(builder.build(), size);
  }

  private ImmutableSetMultimap(
      ImmutableMap<K, ImmutableSet<V>> map, int size) {
    super(map, size);
  }

  // views

  /**
   * Returns an immutable set of the values for the given key.  If no mappings
   * in the multimap have the provided key, an empty immutable set is returned.
   * The values are in the same order as the parameters used to build this
   * multimap.
   */
  public ImmutableSet<V> get(@Nullable K key) {
    // This cast is safe as its type is known in constructor.
    ImmutableSet<V> set = (ImmutableSet<V>) map.get(key);
    return (set == null) ? ImmutableSet.<V>of() : set;
  }

  /**
   * Guaranteed to throw an exception and leave the multimap unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public ImmutableSet<V> removeAll(Object key) {
    throw new UnsupportedOperationException();
  }

  /**
   * Guaranteed to throw an exception and leave the multimap unmodified.
   *
   * @throws UnsupportedOperationException always
   */
  public ImmutableSet<V> replaceValues(K key, Iterable<? extends V> values) {
    throw new UnsupportedOperationException();
  }

  private transient ImmutableSet<Map.Entry<K, V>> entries;

  /**
   * Returns an immutable collection of all key-value pairs in the multimap.
   * Its iterator traverses the values for the first key, the values for the
   * second key, and so on.
   */
  // TODO: Fix this so that two copies of the entries are not created.
  @Override public ImmutableSet<Map.Entry<K, V>> entries() {
    ImmutableSet<Map.Entry<K, V>> result = entries;
    return (result == null)
        ? (entries = ImmutableSet.copyOf(super.entries()))
        : result;
  }

  /**
   * @serialData number of distinct keys, and then for each distinct key: the
   *     key, the number of values for that key, and the key's values
   */
  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject();
    Serialization.writeMultimap(this, stream);
  }

  private void readObject(ObjectInputStream stream)
      throws IOException, ClassNotFoundException {
    stream.defaultReadObject();
    int keyCount = stream.readInt();
    if (keyCount < 0) {
      throw new InvalidObjectException("Invalid key count " + keyCount);
    }
    ImmutableMap.Builder<Object, ImmutableSet<Object>> builder
        = ImmutableMap.builder();
    int tmpSize = 0;

    for (int i = 0; i < keyCount; i++) {
      Object key = stream.readObject();
      int valueCount = stream.readInt();
      if (valueCount <= 0) {
        throw new InvalidObjectException("Invalid value count " + valueCount);
      }

      Object[] array = new Object[valueCount];
      for (int j = 0; j < valueCount; j++) {
        array[j] = stream.readObject();
      }
      ImmutableSet<Object> valueSet = ImmutableSet.of(array);
      if (valueSet.size() != array.length) {
        throw new InvalidObjectException(
            "Duplicate key-value pairs exist for key " + key);
      }
      builder.put(key, valueSet);
      tmpSize += valueCount;
    }

    ImmutableMap<Object, ImmutableSet<Object>> tmpMap;
    try {
      tmpMap = builder.build();
    } catch (IllegalArgumentException e) {
      throw (InvalidObjectException)
          new InvalidObjectException(e.getMessage()).initCause(e);
    }

    FieldSettersHolder.MAP_FIELD_SETTER.set(this, tmpMap);
    FieldSettersHolder.SIZE_FIELD_SETTER.set(this, tmpSize);
  }

  private static final long serialVersionUID = 0;
}
