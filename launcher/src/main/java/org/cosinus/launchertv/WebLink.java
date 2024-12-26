/*
 * Simple TV Launcher
 * Copyright 2017 Alexandre Del Bigio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cosinus.launchertv;


import android.support.annotation.NonNull;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;


public class WebLink {
	private int mIcon;
	private String mName;
	private final String mLink;

	public WebLink( String mLink, @DrawableRes int res, String mName) {
		this.mIcon = res;
		this.mName = mName;
		this.mLink = mLink;
	}


	@NonNull
	public String getName() {
		if (mName != null)
			return mName;
		return ("");
	}

	public int getIcon() {
		return mIcon;
	}

	public String getPackageName() {
		return mLink;
	}
}
