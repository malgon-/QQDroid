/*
 * This file is part of QQDroid.

 * QQDroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * QQDroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with QQDroid.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.pixellostudio.qqdroid;

import roboguice.util.temp.Ln;
import android.app.Application;
import android.util.Log;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.binary.InFileInputStreamObjectPersister;
import com.octo.android.robospice.persistence.string.InFileStringObjectPersister;

public class RoboSpiceService extends SpiceService {

	@Override
	public CacheManager createCacheManager(Application application) {
		CacheManager cacheManager = new CacheManager();

		Ln.getConfig().setLoggingLevel(Log.ERROR);

		// init
		InFileStringObjectPersister inFileStringObjectPersister = new InFileStringObjectPersister(
				application);
		InFileInputStreamObjectPersister inFileInputStreamObjectPersister = new InFileInputStreamObjectPersister(
				application);

		inFileStringObjectPersister.setAsyncSaveEnabled(true);
		inFileInputStreamObjectPersister.setAsyncSaveEnabled(true);

		cacheManager.addPersister(inFileStringObjectPersister);
		cacheManager.addPersister(inFileInputStreamObjectPersister);
		return cacheManager;
	}

}