package org.eclipse.emf.common.util;

import org.eclipse.emf.common.util.Pool.AccessUnit;

class QueuePlateform extends Queue
  {
    private static final long serialVersionUID = 1L;

    @Override
    public PlatformAccessUnit pop(boolean isExclusive)
    {
      return (PlatformAccessUnit)super.pop(isExclusive);
    }

    @Override
    protected AccessUnit<URI> newAccessUnit()
    {
      return new PlatformAccessUnit(this);
    }
  }