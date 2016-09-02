// IOnNewBookArrivedListener.aidl
package com.quan.administrator.myapp1.aidl;

// Declare any non-default types here with import statements
import com.quan.administrator.myapp1.aidl.Book;
interface IOnNewBookArrivedListener {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
    void onNewBookArrived(in Book newBook);
}
