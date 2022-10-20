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
package io.github.mzmine.gui.chartbasics.gui.wrapper;

import io.github.mzmine.gui.chartbasics.gestures.ChartGestureHandler;
import io.github.mzmine.gui.chartbasics.gestures.ChartGesture.GestureButton;
import io.github.mzmine.gui.chartbasics.gestures.ChartGesture.Entity;
import io.github.mzmine.gui.chartbasics.gestures.ChartGesture.Event;
import io.github.mzmine.gui.chartbasics.gestures.ChartGesture.Key;
import io.github.mzmine.gui.chartbasics.gestures.ChartGestureDragDiffHandler.Orientation;
import io.github.mzmine.gui.chartbasics.gestures.ChartGestureHandler.DragHandler;
import io.github.mzmine.gui.chartbasics.gestures.ChartGestureHandler.Handler;

public interface GestureMouseAdapter {

  /**
   * Add drag handlers for each key (key and handler have to be ordered)
   * 
   * @param g
   * @param handler
   */
  public void addDragGestureHandler(DragHandler[] handler, Key[] key, Entity entity, GestureButton button,
      Orientation orient, Object[] param);

  /**
   * Add a preset handler for specific gestures and ChartMouseGestureEvents
   * 
   * @param g
   * @param handler
   */
  public void addGestureHandler(Handler handler, Entity entity, Event[] event, GestureButton button,
      Key key, Object[] param);

  /**
   * Add a handler for specific gestures and ChartMouseGestureEvents
   * 
   * @param g
   * @param handler
   */
  public void addGestureHandler(ChartGestureHandler handler);

  /**
   * Add a handler for specific gestures and ChartMouseGestureEvents
   * 
   * @param g
   * @param handler
   */
  public void removeGestureHandler(ChartGestureHandler handler);
}
