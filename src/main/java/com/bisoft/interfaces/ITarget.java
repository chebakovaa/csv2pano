package com.bisoft.interfaces;

import com.bisoft.interfaces.IClearedTarget;
import com.bisoft.navi.common.exceptions.LoadResourceException;

public interface ITarget {
	IClearedTarget clearedTarget() throws LoadResourceException;
}
