/*
 * Copyright 2006-2007 The MZmine Development Team
 * 
 * This file is part of MZmine.
 * 
 * MZmine is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.alignment.join;

import net.sf.mzmine.data.Peak;

class PeakWrapper {

    private Peak Peak;
    private boolean alreadyJoined;

    public PeakWrapper(Peak Peak) {
        this.Peak = Peak;
        alreadyJoined = false;
    }

    public Peak getPeak() {
        return Peak;
    }

    public boolean isAlreadyJoined() {
        return alreadyJoined;
    }

    public void setAlreadyJoined(boolean alreadyJoined) {
        this.alreadyJoined = alreadyJoined;
    }
}
