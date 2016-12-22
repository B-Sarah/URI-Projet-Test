package org.eclipse.emf.common.util;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

/**
   * A weak reference for the external queue that when cleared will 
   */
  class LazyFragmentInitializer extends WeakReference<Fragment>
  {
    protected final String fragment;

    public LazyFragmentInitializer(Fragment uri, ReferenceQueue<? super URI> queue, String fragment)
    {
      super(uri, queue);
      this.fragment = fragment;
      enqueue();
    }

    @Override
    public void clear()
    {
      Fragment uri = get();
      if (uri != null)
      {
        uri.fragment = URI.splitInternFragment(fragment);
        uri.hashCode = (uri.uri.hashCode * 31 + URI.FRAGMENT_SEPARATOR) * CommonUtil.powerOf31(fragment.length()) + uri.fragment.hashCode();
      }
    }
  }