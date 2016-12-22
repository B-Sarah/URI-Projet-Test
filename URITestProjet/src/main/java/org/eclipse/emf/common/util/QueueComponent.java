package org.eclipse.emf.common.util;

import org.eclipse.emf.common.util.Pool.AccessUnit;

class QueueComponent extends Queue
  {
    private static final long serialVersionUID = 1L;

    @Override
    public URIComponentsAccessUnit pop(boolean isExclusive)
    {
      return (URIComponentsAccessUnit)super.pop(isExclusive);
    }

    @Override
    protected AccessUnit<URI> newAccessUnit()
    {
      return new URIComponentsAccessUnit(this);
    }
  }