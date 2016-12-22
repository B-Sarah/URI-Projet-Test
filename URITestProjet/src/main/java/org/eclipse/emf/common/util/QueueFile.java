package org.eclipse.emf.common.util;

import org.eclipse.emf.common.util.Pool.AccessUnit;

class QueueFile extends AccessUnit.Queue<URI>
  {
    private static final long serialVersionUID = 1L;

    @Override
    public FileAccessUnit pop(boolean isExclusive)
    {
      return (FileAccessUnit)super.pop(isExclusive);
    }

    @Override
    protected AccessUnit<URI> newAccessUnit()
    {
      return new FileAccessUnit(this);
    }
  }