package org.eclipse.emf.common.util;

import org.eclipse.emf.common.util.Pool.AccessUnit;

class QueueString extends Queue
  {
    private static final long serialVersionUID = 1L;

    final protected URIPool pool;

    public QueueString(URIPool pool)
    {
      this.pool = pool;
    }

    @Override
    public StringAccessUnit pop(boolean isExclusive)
    {
      return (StringAccessUnit)super.pop(isExclusive);
    }

    @Override
    protected AccessUnit<URI> newAccessUnit()
    {
      return new StringAccessUnit(this, pool);
    }
  }