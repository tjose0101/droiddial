/*
 * Copyright (c) Oliver Bye 2010
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.xpdeveloper.dialer.common.model;

public class DialMemento {
	private String _dialString;
	private IToneDialModel _model;

	public DialMemento(IToneDialModel toneDialModel, String adjustNumber) {
		_model = toneDialModel;
		_dialString = adjustNumber;
	}

	public void dial() throws InterruptedException {
		_model.dial(this);
	}

	public String getDialString() {
		return _dialString;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((_dialString == null) ? 0 : _dialString.hashCode());
		result = prime * result + ((_model == null) ? 0 : _model.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DialMemento other = (DialMemento) obj;
		if (_dialString == null) {
			if (other._dialString != null)
				return false;
		} else if (!_dialString.equals(other._dialString))
			return false;
		if (_model == null) {
			if (other._model != null)
				return false;
		} else if (!_model.equals(other._model))
			return false;
		return true;
	}
}