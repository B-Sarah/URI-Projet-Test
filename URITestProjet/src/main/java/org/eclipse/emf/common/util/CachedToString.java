package org.eclipse.emf.common.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
 * A specialized weak reference used by {@link URI#toString} that removes the URI's reference when {@link #clear()} is called.
 *
 */
class CachedToString extends WeakReference<String>
{
  protected final URI uri;

  public CachedToString(URI uri, String string, ReferenceQueue<? super String> queue)
  {
    super(string, queue);
    this.uri = uri;
  }

  @Override
  public void clear()
  {
    uri.flushCachedString();

    super.clear();
  }
}