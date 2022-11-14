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

package io.github.mzmine.parameters.parametertypes.ranges;

public class MZRangeParameter extends DoubleRangeParameter {

  public MZRangeParameter() {
    super("m/z", "m/z range", null, true, null);
  }

  public MZRangeParameter(boolean valueRequired) {
    super("m/z", "m/z range", null, valueRequired, null);
  }

  public MZRangeParameter(String name, String description) {
    super(name, description, null, true, null);
  }

  @Override
  public MZRangeComponent createEditingComponent() {
    return new MZRangeComponent();
  }

  @Override
  public MZRangeParameter cloneParameter() {
    MZRangeParameter copy = new MZRangeParameter(getName(), getDescription());
    copy.setValue(this.getValue());
    return copy;
  }

}
