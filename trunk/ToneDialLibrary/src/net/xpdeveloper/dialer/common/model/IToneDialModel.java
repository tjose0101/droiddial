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


public interface IToneDialModel {

	/**
	 * Localise the dialString into a memento
	 * @param dialString
	 *            the tones to dial (including " " and "-" for pauses
	 */
	public DialMemento localise(String dialString);

	/**
	 * @param memento result from localise
	 * @throws InterruptedException
	 *             because we Object.wait() for tones to play.
	 */
	public void dial(DialMemento memento)
	throws InterruptedException;
	
	/**
	 * Free up OS resources, e.g. the internal ToneGenerator that my
	 * implementation is highly likely to contain
	 */
	public void release();
}