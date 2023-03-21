/*
 * Copyright (c) 2004-2022 The MZmine Development Team
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package io.github.mzmine.datamodel.features;

import com.google.common.collect.Streams;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.jetbrains.annotations.Nullable;

/**
 * Iterate over all annotations in a feature list row
 */
public class FeatureAnnotationIterator implements Iterator<Object> {

  private final FeatureAnnotationPriority[] includedLevels;
  private final FeatureListRow row;
  private List<?> nextList;
  private List<?> currentList;
  private int currentLevel = -1;
  private int currentIndex = -1;

  public FeatureAnnotationIterator(final FeatureListRow row) {
    this(row, FeatureAnnotationPriority.values());
  }

  public FeatureAnnotationIterator(final FeatureListRow row,
      FeatureAnnotationPriority[] includedLevels) {
    this.row = row;
    this.includedLevels = includedLevels;
  }

  @Nullable
  private List<?> getNextAnnotations() {
    currentLevel++;
    for (; currentLevel < includedLevels.length; currentLevel++) {
      List<?> all = includedLevels[currentLevel].getAll(row);
      if (!all.isEmpty()) {
        return all;
      }
    }
    return null;
  }

  @Override
  public boolean hasNext() {
    if (currentList != null && currentIndex + 1 < currentList.size()) {
      return true;
    }

    if (nextList == null) {
      nextList = getNextAnnotations();
    }
    return nextList != null;
  }

  @Override
  public Object next() {
    currentIndex++;
    if (currentList != null && currentIndex < currentList.size()) {
      return currentList.get(currentIndex);
    }

    currentIndex = 0;
    currentList = nextList == null ? getNextAnnotations() : nextList;
    nextList = null;

    if (currentList != null && currentIndex < currentList.size()) {
      return currentList.get(currentIndex);
    }

    return null;
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Cannot remove annotation like this");
  }

  public Stream<Object> stream() {
    return Streams.stream(this);
  }
}
